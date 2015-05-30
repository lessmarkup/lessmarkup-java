/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.configuration

import java.util.logging.Level

import com.google.gson.{JsonElement, JsonObject}
import com.google.inject.Inject
import com.lessmarkup.dataobjects.Node
import com.lessmarkup.framework.helpers.{DependencyResolver, JsonSerializer, LanguageHelper, LoggingHelper, StringHelper}
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.annotations.{NodeAccessType, InputField, InputFieldType}
import com.lessmarkup.interfaces.cache.{DataCache, EntityChangeType}
import com.lessmarkup.interfaces.data._
import com.lessmarkup.interfaces.exceptions.RecordValidationException
import com.lessmarkup.interfaces.module.ModuleProvider
import com.lessmarkup.interfaces.recordmodel.{EnumSource, InputSource, RecordModel, RecordModelCache, RecordModelDefinition}
import com.lessmarkup.interfaces.structure.NodeHandlerFactory
import com.lessmarkup.userinterface.nodehandlers.configuration.NodeListNodeHandler
import com.lessmarkup.{Constants, TextIds}

import scala.collection.JavaConversions._
import scala.collection.mutable

class NodeSettingsModel @Inject() (moduleProvider: ModuleProvider, domainModelProvider: DomainModelProvider,
                                   dataCache: DataCache, changeTracker: ChangeTracker)
  extends RecordModel[NodeSettingsModel](TextIds.NODE_SETTINGS) with InputSource {

  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.TITLE, required = true)
  var title: String = null
  @InputField(fieldType = InputFieldType.SELECT, textId = TextIds.HANDLER, required = true)
  var handlerId: String = null
  var settings: JsonElement = null
  var settingsModelId: Option[String] = None
  var nodeId: Long = 0L
  var customizable: Boolean = false
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.ENABLED, defaultValue = "true")
  var enabled: Boolean = false
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.ADD_TO_MENU, defaultValue = "false")
  var addToMenu: Boolean = false
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.PATH, required = true)
  var path: String = null
  var position: Int = 0
  var roleText: Option[String] = None
  private final val children: mutable.MutableList[NodeSettingsModel] = new mutable.MutableList()
  var parentId: Option[Long] = None

  def sortChildren = {
    children.sortBy(_.position)
  }

  def getChildren: Seq[NodeSettingsModel] = {
    children.seq
  }

  def getEnumValues(fieldName: String): Seq[EnumSource] = {
    StringHelper.toJsonCase(fieldName) match {
      case "handlerId" =>
        val modules = moduleProvider.getModules.map(_.getModuleType)
        moduleProvider.getNodeHandlers.flatMap(id => {
          val handler = moduleProvider.getNodeHandler(id).get
          if (!modules.contains(handler._2)) {
            None
          } else {
            Option(new EnumSource(id, NodeListNodeHandler.getHandlerName(handler._1, handler._2)))
          }
        })
      case _ => throw new IllegalArgumentException
    }
  }

  def createNode: Option[AnyRef] = {
    val modelCache: RecordModelCache = dataCache.get(classOf[RecordModelCache])
    val definition: RecordModelDefinition = modelCache.getDefinition(classOf[NodeSettingsModel]).get
    try {
      definition.validateInput(JsonSerializer.serializePojoToTree(this), isNew = true)
    }
    catch {
      case ex: RecordValidationException =>
        LoggingHelper.getLogger(getClass).log(Level.SEVERE, null, ex)
        return None
    }

    val target = new Node
    target.enabled = enabled
    target.handlerId = handlerId
    target.parentId = parentId
    target.position = position
    target.path = path
    target.addToMenu = addToMenu
    target.settings = if (settings != null) Option(settings.toString) else None
    target.title = title
    target.description = ""

    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      domainModel.create(target)
      changeTracker.addChange(classOf[Node], target, EntityChangeType.ADDED, domainModel)
      domainModel.completeTransaction()
    } finally {
      if (domainModel != null) domainModel.close()
    }

    nodeId = target.id
    val handlerFactory: NodeHandlerFactory = DependencyResolver(moduleProvider.getNodeHandler(handlerId).get._1)
    val configuration = new NodeHandlerConfiguration(
      objectId = None,
      settings = None,
      accessType = NodeAccessType.READ,
      path = "",
      fullPath = ""
    )
    val handler = handlerFactory.createNodeHandler(configuration)
    customizable = handler.getSettingsModel != null
    if (customizable) {
      settingsModelId = Option(modelCache.getDefinition(handler.getSettingsModel.get).get.getId)
    }
    Option(this)
  }

  def updateNode(): Option[AnyRef] = {
    val modelCache: RecordModelCache = dataCache.get(classOf[RecordModelCache])
    val definition: RecordModelDefinition = modelCache.getDefinition(classOf[NodeSettingsModel]).get
    try {
      definition.validateInput(JsonSerializer.serializePojoToTree(this), isNew = false)
    }
    catch {
      case ex: RecordValidationException =>
        LoggingHelper.getLogger(getClass).log(Level.SEVERE, null, ex)
    }
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      val record: Node = domainModel.query.find(classOf[Node], nodeId).get
      record.title = title
      record.parentId = parentId
      record.path = path
      record.enabled = enabled
      record.addToMenu = addToMenu
      domainModel.update(record)
      changeTracker.addChange(classOf[Node], record, EntityChangeType.UPDATED, domainModel)
      domainModel.completeTransaction()
    } finally {
      if (domainModel != null) domainModel.close()
    }
    Option(this)
  }

  def deleteNode(): AnyRef = {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      val node: Node = domainModel.query.find(classOf[Node], nodeId).get
      val nodeParentId = node.parentId
      domainModel.delete(classOf[Node], node.id)
      changeTracker.addChange(classOf[Node], node, EntityChangeType.REMOVED, domainModel)
      val nodes = domainModel
        .query
        .from(classOf[Node])
        .where("parentId = $", nodeParentId)
        .toList(classOf[Node])

      nodes.zipWithIndex.filter(n => n._1.position != n._2).foreach {
        case (n, i) =>
          n.position = i
          domainModel.update(n)
          changeTracker.addChange(classOf[Node], n, EntityChangeType.UPDATED, domainModel)
      }

      domainModel.completeTransaction()
    } finally {
      if (domainModel != null) domainModel.close()
    }
    null
  }

  private def normalizeTree(nodes: Seq[NodeSettingsModel], rootNode: NodeSettingsModel, domainModel: DomainModel, changedNodes: mutable.Set[Long]) {
    nodes.foreach(_.sortChildren)
    if (rootNode != null) {
      nodes.filter(n => n.nodeId != rootNode.nodeId && n.parentId.isEmpty)
        .foreach(node => {
          rootNode.getChildren.add(node)
          node.parentId = Option(rootNode.nodeId)
          val record = domainModel.query.find(classOf[Node], node.nodeId)
          if (record.get.parentId.get != node.parentId.get) {
            record.get.parentId = node.parentId
            domainModel.update(record.get)
          }

          changedNodes.add(node.nodeId)
      })
    }

    for (node <- nodes) {
      node.getChildren.zipWithIndex.filter(n => n._1.position != n._2).foreach {
        case (child, i) =>
          child.position = i
          val record: Node = domainModel.query.find(classOf[Node], child.nodeId).get
          if (record.position != i) {
            record.position = i
            domainModel.update(record)
          }
          changedNodes.add(record.id)
      }
    }
  }

  def getRootNode: NodeSettingsModel = {
    var rootNode: NodeSettingsModel = null
    val modelCache: RecordModelCache = dataCache.get(classOf[RecordModelCache])
    val domainModel: DomainModel = domainModelProvider.create
    try {

      val nodes: Seq[NodeSettingsModel] = domainModel.query.from(classOf[Node])
        .toList(classOf[Node])
        .sortBy(_.position)
        .map(source => {

        val node = DependencyResolver(classOf[NodeSettingsModel])

        val handlerReference = moduleProvider.getNodeHandler(source.handlerId)
        val handler =
          if (handlerReference.isDefined) {
            val factory = DependencyResolver(handlerReference.get._1)

            val configuration = new NodeHandlerConfiguration(
              objectId = None,
              settings = None,
              accessType = NodeAccessType.READ,
              path = "",
              fullPath = ""
            )

            Option(factory.createNodeHandler(configuration))
          }
          else None

        node.parentId = source.parentId
        node.enabled = source.enabled
        node.handlerId = source.handlerId
        node.nodeId = source.id
        node.addToMenu = source.addToMenu
        node.position = source.position

        if (handler.isDefined && handler.get.getSettingsModel.isDefined) {
          if (source.settings.isDefined) {
            node.settings = JsonSerializer.deserializeToTree(source.settings.get)
          }
          node.settingsModelId = Option(modelCache.getDefinition(handler.get.getSettingsModel.get).get.getId)
        }
        node.title = source.title
        node.path = source.path
        node.customizable = node.settingsModelId.isDefined

        node
      })

      val nodeIds = nodes.map(n => (n.nodeId, n)).toMap

      val changedNodes: mutable.Set[Long] = mutable.Set()

      for (node <- nodes if node.parentId.isDefined) {

        val parent = nodeIds.get(node.parentId.get)

        if (parent.isEmpty) {
          node.parentId = None
          val record = domainModel.query.find(classOf[Node], node.nodeId).get
          record.parentId = None
          domainModel.update(record)
          changedNodes.add(node.nodeId)
        }
        else {
          parent.get.getChildren.add(node)
        }
      }
      val r = nodes.find(_.parentId.isEmpty)
      rootNode = if (r.isDefined) r.get else null
      normalizeTree(nodes, rootNode, domainModel, changedNodes)
      changedNodes.foreach(id => changeTracker.addChange(classOf[Node], id, EntityChangeType.UPDATED, domainModel) )
    } finally {
      if (domainModel != null) domainModel.close()
    }
    rootNode
  }

  def changeSettings: AnyRef = {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      val node: Node = domainModel.query.find(classOf[Node], nodeId).get
      node.settings = if (settings != null) settings.toString else null
      domainModel.update(node)
      changeTracker.addChange(classOf[Node], node, EntityChangeType.UPDATED, domainModel)
      domainModel.completeTransaction()
    } finally {
      if (domainModel != null) domainModel.close()
    }
    settings
  }

  def updateParent(): JsonElement = {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      val node: Node = domainModel.query.find(classOf[Node], nodeId).get
      val changedNodes: mutable.Set[Long] = mutable.Set()
      if (node.parentId.isDefined != parentId.isDefined) {
        var newRootNode: Node = null
        var oldRootNode: Node = null
        if (node.parentId.isDefined) {
          newRootNode = node
          oldRootNode = domainModel.query.from(classOf[Node]).where("parentId IS NULL").first(classOf[Node], None).get
        }
        else {
          oldRootNode = node
          newRootNode = domainModel.query.find(classOf[Node], parentId.get).get
        }
        if (!newRootNode.parentId.isDefined) {
          throw new IllegalArgumentException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.CANNOT_HAVE_TWO_ROOT_NODES))
        }
        for (neighbor <- domainModel.query.from(classOf[Node])
            .where("parentId = $ AND position > $", newRootNode.parentId.get, newRootNode.position)
            .toList(classOf[Node])) {
          neighbor.position = neighbor.position - 1
          domainModel.update(neighbor)
          changedNodes.add(neighbor.id)
        }
        newRootNode.position = 0
        newRootNode.parentId = None
        changedNodes.add(newRootNode.id)
        for (neighbor <- domainModel.query.from(classOf[Node])
            .where("parentId = $ AND " + Constants.DataIdPropertyName + " != $ AND position > 0", newRootNode.id, nodeId)
            .toList(classOf[Node])) {
          neighbor.position = neighbor.position + 1
          domainModel.update(neighbor)
          changedNodes.add(neighbor.id)
        }
        oldRootNode.position = 0
        oldRootNode.parentId = newRootNode.id
        changedNodes.add(oldRootNode.id)
      }
      else if (node.parentId.isDefined) {
        if (node.parentId.get == parentId.get) {
          if (position > node.position) {
            for (neighbor <- domainModel.query.from(classOf[Node]).where("parentId = $ AND " + Constants.DataIdPropertyName + " != $ AND position > $ AND position <= $", parentId, nodeId, node.position, position).toList(classOf[Node])) {
              neighbor.position = neighbor.position - 1
              domainModel.update(neighbor)
              changedNodes.add(neighbor.id)
            }
          }
          else if (position < node.position) {
            for (neighbor <- domainModel.query.from(classOf[Node]).where("parentId = $ AND " + Constants.DataIdPropertyName + " != $ AND position >= $ AND position < $", parentId, nodeId, position, node.position).toList(classOf[Node])) {
              neighbor.position = neighbor.position + 1
              domainModel.update(neighbor)
              changedNodes.add(neighbor.id)
            }
          }
        }
        else {
          for (neighbor <- domainModel.query.from(classOf[Node]).where("parentId = $ AND position > $ AND " + Constants.DataIdPropertyName + " != $", node.parentId, node.position, nodeId).toList(classOf[Node])) {
            neighbor.position = neighbor.position - 1
            domainModel.update(neighbor)
            changedNodes.add(neighbor.id)
          }
          for (neighbor <- domainModel.query.from(classOf[Node]).where("parentId = $ AND position >= $ AND " + Constants.DataIdPropertyName + " != $", parentId, position, nodeId).toList(classOf[Node])) {
            neighbor.position = neighbor.position + 1
            domainModel.update(neighbor)
            changedNodes.add(neighbor.id)
          }
        }
      }
      node.parentId = parentId
      node.position = position
      changedNodes.add(nodeId)
      for (id <- changedNodes) {
        changeTracker.addChange(classOf[Node], id, EntityChangeType.UPDATED, domainModel)
      }
      domainModel.completeTransaction()
    } finally {
      if (domainModel != null) domainModel.close()
    }
    val rootNode: JsonElement = JsonSerializer.serializePojoToTree(getRootNode)
    val ret: JsonObject = new JsonObject
    ret.add("Root", rootNode)
    ret
  }
}