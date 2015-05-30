/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.common

import java.lang.reflect.Modifier

import com.google.gson.{JsonArray, JsonElement, JsonObject, JsonPrimitive}
import com.lessmarkup.{Constants, TextIds}
import com.lessmarkup.framework.helpers._
import com.lessmarkup.framework.nodehandlers.{AbstractNodeHandler, NodeHandlerConfiguration}
import com.lessmarkup.interfaces.annotations.{ActionAccess, NodeAccessType, Parameter, RecordAction, RecordSearch, SupportsTextSearch}
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data._
import com.lessmarkup.interfaces.recordmodel._
import com.lessmarkup.interfaces.system.{ResourceCache, SiteConfiguration, UserCache}

object RecordListNodeHandler {

  object ActionType extends Enumeration {
    type ActionType = Value
    val RECORD, CREATE, RECORD_CREATE, RECORD_INITIALIZE_CREATE = Value
  }

  class Link(val text: String, val url: String, val external: Boolean)

  class Action(val text: String, val name: String, val visible: String, val parameter: String, val actionType: ActionType.Value)

  private def getColumnWidth(column: RecordColumnDefinition): String = {
    if (!StringHelper.isNullOrEmpty(column.width)) {
      return column.width
    }
    "*"
  }
}

abstract class RecordListNodeHandler[T <: RecordModel[_]](
    domainModelProvider: DomainModelProvider,
    dataCache: DataCache,
    modelType: Class[T],
    configuration: NodeHandlerConfiguration,
    columnUrls: Map[String, String] = Map.empty,
    links: Seq[RecordListNodeHandler.Link] = Seq.empty
    )
  extends AbstractNodeHandler(configuration) {

  private val actions: Seq[RecordListNodeHandler.Action] = createEditActions ++ createActionsFromMethods
  private val allProperties: Iterable[PropertyDescriptor] = TypeHelper.getProperties(modelType)
  protected val modelCollection: ModelCollection[T] = {
    val ret = createCollection
    if (ret == null) {
      throw new IllegalArgumentException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.MISSING_PARAMETER))
    }
    ret
  }
  private val editableModelCollection: EditableModelCollection[T] = if (modelCollection.isInstanceOf[EditableModelCollection[_]]) modelCollection.asInstanceOf[EditableModelCollection[T]] else null
  private val recordModel: RecordModelDefinition = {
    val formCache: RecordModelCache = dataCache.get(classOf[RecordModelCache])
    val ret = formCache.getDefinition(modelType).get
    if (recordModel == null) {
      throw new IllegalArgumentException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.MISSING_PARAMETER, modelType.getName))
    }
    ret
  }

  protected def getRecordModel: RecordModelDefinition = {
    recordModel
  }

  protected def createEditActions: Seq[RecordListNodeHandler.Action] = {
    if (editableModelCollection != null) {
      if (editableModelCollection.isDeleteOnly) {
        val removeRecordAction = createRecordAction("removeRecord", Constants.ModuleTypeMain, TextIds.REMOVE_RECORD, null)
        removeRecordAction :: Nil
      }
      else {
        val addRecordAction = createCreateAction("addRecord", Constants.ModuleTypeMain, TextIds.ADD_RECORD, modelType)
        val modifyRecordAction = createRecordAction("modifyRecord", Constants.ModuleTypeMain, TextIds.MODIFY_RECORD, null)
        val removeRecordAction = createRecordAction("removeRecord", Constants.ModuleTypeMain, TextIds.REMOVE_RECORD, null)

        addRecordAction :: modifyRecordAction :: removeRecordAction :: Nil
      }
    } else {
      Nil
    }
  }

  private def createActionsFromMethods: Seq[RecordListNodeHandler.Action] = {

    val modelCache: RecordModelCache = dataCache.get(classOf[RecordModelCache])

    val ret: Seq[RecordListNodeHandler.Action] = for (
      method <- modelType.getMethods;
      actionAttribute = method.getAnnotation(classOf[RecordAction])
      if (method.getModifiers & Modifier.STATIC) == 0
      if actionAttribute != null
      if actionAttribute.minimumAccess != NodeAccessType.NO_ACCESS || getAccessType.getLevel >= actionAttribute.minimumAccess.getLevel;
      parameters = method.getParameterTypes
      if parameters.length == 1 && parameters(0) == classOf[Long]
    ) yield {

      val isObject = actionAttribute.createType == classOf[AnyRef]

      new RecordListNodeHandler.Action(
        text = LanguageHelper.getFullTextId(recordModel.getModuleType, actionAttribute.nameTextId),
        visible = actionAttribute.visible(),
        actionType = if (isObject) RecordListNodeHandler.ActionType.RECORD_CREATE else if (actionAttribute.initialize) RecordListNodeHandler.ActionType.RECORD_INITIALIZE_CREATE else RecordListNodeHandler.ActionType.RECORD_CREATE,
        parameter = if (isObject) null else modelCache.getDefinition(actionAttribute.createType).get.getId,
        name = ""
      )
    }

    ret
  }

  protected def getEditableCollection: EditableModelCollection[T] = editableModelCollection

  protected def createCollection: ModelCollection[T] = {
    recordModel.createModelCollection.asInstanceOf[ModelCollection[T]]
  }

  override def getViewType: String = "RecordList"

  protected def isSupportsLiveUpdates: Boolean = true

  protected def isSupportsManualRefresh: Boolean = false

  protected def createRecordLink(text: String, url: String, external: Boolean) : RecordListNodeHandler.Link = {
    new RecordListNodeHandler.Link(
      text = LanguageHelper.getFullTextId(recordModel.getModuleType, text),
      url = url,
      external = external
    )
  }

  protected def createRecordAction(name: String, moduleType: String, text: String, visible: String): RecordListNodeHandler.Action = {
    new RecordListNodeHandler.Action(
      text = LanguageHelper.getFullTextId(moduleType, text),
      name = name,
      visible = visible,
      actionType = RecordListNodeHandler.ActionType.RECORD,
      parameter = null
    )
  }

  protected def createCreateAction(name: String, moduleType: String, text: String, actionType: Class[T]): RecordListNodeHandler.Action = {
    val typeId: String = if (actionType == null) null else dataCache.get(classOf[RecordModelCache]).getDefinition(actionType).get.getId
    new RecordListNodeHandler.Action(
      text = LanguageHelper.getFullTextId(moduleType, text),
      name = name,
      visible = null,
      actionType = RecordListNodeHandler.ActionType.CREATE,
      parameter = typeId
    )
  }

  protected def getColumnLink(field: String): String = {
    columnUrls.get(field).get
  }

  override def getViewData: Option[JsonObject] = {
    val siteConfiguration: SiteConfiguration = dataCache.get(classOf[SiteConfiguration])
    val recordsPerPage: Int = siteConfiguration.recordsPerPage
    val resourceCache: ResourceCache = dataCache.get(classOf[ResourceCache])
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val data: JsonObject = new JsonObject
      data.addProperty("recordsPerPage", recordsPerPage)
      data.addProperty("type", recordModel.getId)
      data.addProperty("extensionScript", getExtensionScript)
      data.addProperty("recordId", StringHelper.toJsonCase(Constants.DataIdPropertyName))
      data.addProperty("liveUpdates", isSupportsLiveUpdates)
      data.addProperty("manualRefresh", isSupportsManualRefresh)

      val hasSearch = allProperties.exists(_.getAnnotation(classOf[RecordSearch]) != null)
      data.addProperty("getHasSearch", hasSearch)

      val actionsArray: JsonArray = new JsonArray
      for (a <- actions) {
        val o: JsonObject = new JsonObject
        o.addProperty("name", StringHelper.toJsonCase(a.name))
        o.addProperty("text", a.text)
        o.addProperty("visible", a.visible)
        o.addProperty("type", a.actionType.toString)
        o.addProperty("parameter", a.parameter)
        actionsArray.add(o)
      }
      data.add("actions", actionsArray)

      val linksArray: JsonArray = new JsonArray
      for (link <- links) {
        val o: JsonObject = new JsonObject
        o.addProperty("text", link.text)
        o.addProperty("url", link.url)
        o.addProperty("external", link.external)
        linksArray.add(o)
      }
      data.add("links", linksArray)

      data.addProperty("optionsTemplate", resourceCache.parseText("views/recordOptions.html").get)

      val columns: JsonArray = new JsonArray
      for (c <- recordModel.getColumns) {
        val o: JsonObject = new JsonObject
        o.addProperty("width", RecordListNodeHandler.getColumnWidth(c))
        o.addProperty("name", StringHelper.toJsonCase(c.property.getName))
        o.addProperty("text", LanguageHelper.getFullTextId(recordModel.getModuleType, c.textId))
        val link: String = getColumnLink(c.property.getName)
        o.addProperty("url", if (link != null) link else c.cellUrl)
        o.addProperty("sortable", c.sortable)
        o.addProperty("template", c.cellTemplate)
        o.addProperty("cellClick", c.cellUrl)
        o.addProperty("allowUnsafe", c.allowUnsafe)
        o.addProperty("cellClass", c.cellClass)
        o.addProperty("headerClass", c.headerClass)
        o.addProperty("scope", c.scope)
        o.addProperty("align", c.align.toString)
        o.addProperty("ignoreOptions", c.ignoreOptions)
        columns.add(o)
      }
      data.add("columns", columns)
      readRecordsAndIds(data, domainModel, recordsPerPage)
      Option(data)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  protected def getIndex(modifiedObject: T, filter: String, domainModel: DomainModel): Int = {
    var query: QueryBuilder = domainModel.query
    if (recordModel != null && recordModel.getDataType != null) {
      query = applyFilterAndOrderBy(domainModel.query, filter)
    }
    val recordIds: Seq[Long] = modelCollection.readIds(query, ignoreOrder = false)
    val recordId: Long = modifiedObject.id
    recordIds.indexOf(recordId)
  }

  protected def getIndex(modifiedObject: T, filter: String): Int = {
    val domainModel: DomainModel = domainModelProvider.create
    try {
      getIndex(modifiedObject, filter, domainModel)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  @ActionAccess(minimumAccess = NodeAccessType.WRITE)
  def modifyRecord(@Parameter("modifiedObject") modifiedObject: JsonElement, @Parameter("filter") filter: String): JsonObject = {
    recordModel.validateInput(modifiedObject, isNew = false)
    val deserializedModifiedObject: Option[T] = JsonSerializer.deserializePojo(modelType, modifiedObject)
    getEditableCollection.updateRecord(deserializedModifiedObject.get)
    returnRecordResult(deserializedModifiedObject.get, isNew = false, getIndex(deserializedModifiedObject.get, filter))
  }

  @ActionAccess(minimumAccess = NodeAccessType.WRITE)
  def createRecord: JsonObject = {
    val collection: EditableModelCollection[T] = getEditableCollection
    val record: T = if (collection != null) collection.createRecord else DependencyResolver(modelType)
    val ret: JsonObject = new JsonObject
    ret.add("record", JsonSerializer.serializePojoToTree(record))
    ret
  }

  @ActionAccess(minimumAccess = NodeAccessType.WRITE)
  def addRecord(@Parameter("newObject") newObject: JsonElement, @Parameter("filter") filter: String, @Parameter("settings") settings: JsonObject): JsonObject = {
    if (newObject == null || newObject.isJsonNull) {
      return returnRecordResult(DependencyResolver(modelType), isNew = false, -1)
    }
    recordModel.validateInput(newObject, isNew = true)
    val deserializedNewObject: Option[T] = JsonSerializer.deserializePojo(modelType, newObject)
    getEditableCollection.addRecord(deserializedNewObject.get)
    returnRecordResult(deserializedNewObject.get, isNew = true, getIndex(deserializedNewObject.get, filter))
  }

  @ActionAccess(minimumAccess = NodeAccessType.WRITE)
  def removeRecord(@Parameter("recordId") recordId: Long, @Parameter("filter") filter: String): JsonObject = {
    getEditableCollection.deleteRecords(Seq(recordId))
    returnRemovedResult
  }

  @ActionAccess(minimumAccess = NodeAccessType.READ) def getRecordIds(filter: String): JsonObject = {
    val collection: ModelCollection[T] = modelCollection
    val recordIds: JsonArray = new JsonArray
    val domainModel: DomainModel = domainModelProvider.create
    try {
      var query: QueryBuilder = domainModel.query
      if (recordModel != null && recordModel.getDataType != null) {
        query = applyFilterAndOrderBy(domainModel.query, filter)
      }
      collection.readIds(query, ignoreOrder = false).foreach(recordId => recordIds.add(new JsonPrimitive(recordId)))
    } finally {
      if (domainModel != null) domainModel.close()
    }

    val ret: JsonObject = new JsonObject
    ret.add("recordIds", recordIds)
    ret
  }

  override def processUpdates(fromVersion: Option[Long], toVersion: Long, returnValues: JsonObject, domainModel: DomainModel, arguments: JsonObject): Boolean = {
    val collection: ModelCollection[T] = modelCollection
    val changesCache: ChangesCache = dataCache.get(classOf[ChangesCache])
    val changes: Seq[DataChange] = changesCache.getCollectionChanges(collection.getCollectionId, if (fromVersion.isDefined) scala.Option.apply(fromVersion.get) else scala.Option.empty, scala.Option.apply(toVersion), scala.Option.empty)
    if (changes == null) {
      return false
    }
    val removed: JsonArray = new JsonArray
    val updated: JsonArray = new JsonArray
    var filter: String = null
    val filterObj: JsonElement = arguments.get("filter")
    if (filterObj != null) {
      filter = filterObj.getAsString
    }

    val query: QueryBuilder =
      if (recordModel != null && recordModel.getDataType != null) {
        applyFilterAndOrderBy(domainModel.query, filter)
      } else {
        domainModel.query
      }

    val recordIds = collection.readIds(query, ignoreOrder = false)
    for (change <- changes) {
      if (!recordIds.contains(change.getEntityId)) {
        if (removed.size == 0) {
          returnValues.add("records_removed", removed)
        }
        removed.add(new JsonPrimitive(change.getEntityId))
      }
      else {
        if (updated.size == 0) {
          returnValues.add("records_updated", updated)
        }
        updated.add(new JsonPrimitive(change.getEntityId))
      }
    }

    removed.size > 0 || updated.size > 0
  }

  private def readRecordsAndIds(values: JsonObject, domainModel: DomainModel, recordsPerPage: Int) {
    val collection: ModelCollection[T] = modelCollection
    val ids: Seq[Long] = collection.readIds(domainModel.query, ignoreOrder = false)
    val recordIds: JsonArray = new JsonArray
    ids.foreach(recordId => recordIds.add(new JsonPrimitive(recordId)))
    values.add("recordIds", recordIds)
    if (recordsPerPage > 0) {
      val readRecordIds = ids.take(recordsPerPage)
      readRecords(values, readRecordIds, domainModel)
    }
  }

  protected def readRecords(values: JsonObject, ids: Seq[Long], domainModel: DomainModel) {
    val array: JsonArray = new JsonArray
    if (ids != null && ids.nonEmpty) {
      val records = modelCollection.read(domainModel.query, ids)
      postProcessRecords(records)
      records.foreach(r => array.add(JsonSerializer.serializePojoToTree(r)))
    }
    values.add("records", array)
  }

  protected def postProcessRecords(records: Seq[T]) {
  }

  protected def returnRemovedResult: JsonObject = {
    val ret: JsonObject = new JsonObject
    ret.addProperty("removed", true)
    ret
  }

  protected def returnRecordResult(recordId: Long, isNew: Boolean, index: Int): JsonObject = {
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val record: T = modelCollection.read(domainModel.query, Seq(recordId)).head
      returnRecordResult(record, isNew, index)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  protected def returnRecordResult(record: T): JsonObject = {
    returnRecordResult(record, isNew = false)
  }

  protected def returnRecordResult(record: T, isNew: Boolean): JsonObject = {
    returnRecordResult(record, isNew, -1)
  }

  protected def returnRecordResult(record: T, isNew: Boolean, index: Int): JsonObject = {
    postProcessRecords(Seq(record))
    val ret: JsonObject = new JsonObject
    ret.add("record", JsonSerializer.serializePojoToTree(record))
    ret.addProperty("isNew", isNew)
    ret.addProperty("index", index)
    ret
  }

  protected def returnNewObjectResult[TN <: AnyRef](record: TN): JsonObject = {
    val ret = new JsonObject
    ret.add("record", JsonSerializer.serializePojoToTree(record))
    ret
  }

  protected def returnMessageResult(message: String): JsonObject = {
    val ret = new JsonObject
    ret.addProperty("message", message)
    ret
  }

  protected def returnResetResult: JsonObject = {
    val ret = new JsonObject
    ret.addProperty("reset", true)
    ret
  }

  protected def returnRedirectResult(url: String): JsonObject = {
    val ret = new JsonObject
    ret.addProperty("redirect", url)
    ret
  }

  @ActionAccess(minimumAccess = NodeAccessType.READ)
  def fetch(@Parameter("ids") ids: List[Long]): JsonObject = {
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val ret: JsonObject = new JsonObject
      readRecords(ret, ids, domainModel)
      ret
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  protected def getExtensionScript: String = {
    null
  }

  def applyFilterAndOrderBy(queryBuilder: QueryBuilder, filter: String): QueryBuilder = {
    if (StringHelper.isNullOrEmpty(filter)) {
      return queryBuilder
    }
    val searchProperties: JsonElement = JsonSerializer.deserializeToTree(filter)

    if (!searchProperties.isJsonObject) {
      return queryBuilder
    }

    val searchPropertiesObject: JsonObject = searchProperties.getAsJsonObject
    val searchObject: JsonElement = searchPropertiesObject.get("search")
    val updatedQueryBuilder = if (searchObject != null) {
      val searchText: String = "%" + searchObject.getAsString + "%"
      val selectedProperties = allProperties
        .filter(p => p.getType != classOf[String])
        .filterNot(p => !dataCache.get(classOf[UserCache]).isAdministrator && p.getAnnotation(classOf[SupportsTextSearch]) == null)

      val filterParams = Seq.fill(selectedProperties.size)(searchText)
      val filterText: String = selectedProperties.map(p => s"${p.getName} LIKE ($$)").mkString(" OR ")

      if (selectedProperties.nonEmpty) {
        queryBuilder.where("(" + filterText + ")", filterParams: _*)
      } else {
        queryBuilder
      }
    } else {
      queryBuilder
    }

    val orderByObject: JsonElement = searchPropertiesObject.get("orderBy")
    val directionObject: JsonElement = searchPropertiesObject.get("direction")

    if (orderByObject == null || directionObject == null) {
      return updatedQueryBuilder
    }

    val orderBy: String = orderByObject.getAsString
    val orderByProperty = allProperties.find(_.getName.equalsIgnoreCase(orderBy))

    if (orderByProperty.isEmpty) {
      return updatedQueryBuilder
    }

    val ascending: Boolean = directionObject.getAsString == "asc"
    if (ascending) {
      updatedQueryBuilder.orderBy(String.format("%s", orderByProperty.get.getName))
    }
    else {
      updatedQueryBuilder.orderBy(String.format("%s", orderByProperty.get.getName))
    }
  }
}
