/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.data

import com.lessmarkup.Constants
import com.lessmarkup.framework.helpers.DependencyResolver
import com.lessmarkup.interfaces.cache.{DataCache, EntityChangeType}
import com.lessmarkup.interfaces.data.{ChangeTracker, DataObject, DomainModel, DomainModelProvider, QueryBuilder}
import com.lessmarkup.interfaces.recordmodel.{EditableModelCollection, RecordModel}

import scala.collection.JavaConversions._

class RecordModelEditableCollection[TM <: RecordModel[_], TD <: DataObject]
  (domainModelProvider: DomainModelProvider,
   dataCache: DataCache,
   changeTracker: ChangeTracker,
   modelType: Class[TM],
   dataType: Class[TD])
  extends EditableModelCollection[TM] {

  private final val properties = dataCache.get(classOf[EditableCollectionCache]).getProperties(modelType, dataType)

  def readIds(query: QueryBuilder, ignoreOrder: Boolean): Seq[Long] = {
    query.from(dataType).toIdList
  }

  def getCollectionId: Int = {
    domainModelProvider.getCollectionId(dataType).get
  }

  protected def updateModel(model: TM, record: TD) {
    for (property <- properties) {
      val data: AnyRef = property.getDataValue(record)
      property.setModelValue(model, data)
    }
  }

  protected def updateData(record: TD, model: TM) {
    for (property <- properties) {
      val data: AnyRef = property.getModelValue(model)
      if (!(data == null) || !(property.getFieldType == classOf[Array[Byte]])) {
        property.setDataValue(record, data)
      }
    }
  }

  def read(queryBuilder: QueryBuilder, ids: Seq[Long]): Seq[TM] = {
    val idsString: Seq[String] = ids.map(id => id.toString)
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val query = String.format(Constants.DataIdPropertyName + " in (%s)", String.join(",", idsString))

      domainModel.query.from(dataType).where(query).toList(dataType).map(record => {
        val model: TM = DependencyResolver.resolve(modelType)
        updateModel(model, record)
        model
      })
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def createRecord: TM = {
    DependencyResolver.resolve(modelType)
  }

  def addRecord(record: TM) {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      val data = dataType.newInstance()
      updateData(data, record)
      domainModel.create(data)
      changeTracker.addChange(dataType, data, EntityChangeType.ADDED, domainModel)
      domainModel.completeTransaction()
      record.id = data.id
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def updateRecord(record: TM) {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      val data = domainModel.query.from(dataType).find(dataType, record.id).get
      updateData(data, record)
      domainModel.update(data)
      changeTracker.addChange(dataType, data, EntityChangeType.UPDATED, domainModel)
      domainModel.completeTransaction()
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def deleteRecords(recordIds: Seq[Long]): Boolean = {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      for (id <- recordIds) {
        domainModel.delete(dataType, id)
        changeTracker.addChange(dataType, id, EntityChangeType.REMOVED, domainModel)
      }
      domainModel.completeTransaction()
    } finally {
      if (domainModel != null) domainModel.close()
    }
    true
  }
}