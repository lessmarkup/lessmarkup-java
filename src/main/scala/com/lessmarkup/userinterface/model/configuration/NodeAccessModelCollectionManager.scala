/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.configuration

import com.google.inject.Inject
import com.lessmarkup.Constants
import com.lessmarkup.dataobjects.{Node, NodeAccess, User, UserGroup}
import com.lessmarkup.interfaces.annotations.UseInstanceFactory
import com.lessmarkup.interfaces.cache.{EntityChangeType, InstanceFactory}
import com.lessmarkup.interfaces.data.{ChangeTracker, DomainModel, DomainModelProvider, QueryBuilder}
import com.lessmarkup.interfaces.recordmodel.EditableModelCollection

class NodeAccessModelCollectionManagerFactory @Inject() (domainModelProvider: DomainModelProvider, changeTracker: ChangeTracker)
  extends InstanceFactory {
  override def createInstance(params: Any*): AnyRef = {
    new NodeAccessModelCollectionManager(domainModelProvider, changeTracker, params.head.asInstanceOf)
  }
}

@UseInstanceFactory(classOf[NodeAccessModelCollectionManagerFactory])
class NodeAccessModelCollectionManager @Inject() (
  domainModelProvider: DomainModelProvider,
  changeTracker: ChangeTracker,
  nodeId: Long)
  extends EditableModelCollection[NodeAccessModel] {

  def readIds(query: QueryBuilder, ignoreOrder: Boolean): Seq[Long] = {
    query.from(classOf[NodeAccess])
      .where("nodeId = $", nodeId).toIdList
  }

  def getCollectionId: Int = {
    domainModelProvider.getCollectionId(classOf[NodeAccess]).get
  }

  def read(query: QueryBuilder, ids: Seq[Long]): Seq[NodeAccessModel] = {
    query.from(classOf[NodeAccess], Option("na"))
      .where(s"na.nodeId = $$ AND na.${Constants.DataIdPropertyName} IN (${ids.map(_.toString).mkString(",")}})", nodeId)
      .leftJoin(classOf[User], "u", "u." + Constants.DataIdPropertyName + " = na.userId")
      .leftJoin(classOf[UserGroup], "g", "g." + Constants.DataIdPropertyName + " = na.groupId")
      .toList(classOf[NodeAccessModel], Option("na.accessType, u.email, g.name, na." + Constants.DataIdPropertyName + " AccessId"))
  }

  def isFiltered: Boolean = false

  def createRecord: NodeAccessModel = {
    new NodeAccessModel
  }

  def addRecord(record: NodeAccessModel) {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {

      val access = new NodeAccess
      access.accessType = record.accessType
      access.nodeId = nodeId

      if (record.user != null && record.group.length > 0) {
        val userId: Long = domainModel.query
          .from(classOf[User])
          .where("email = $", record.user)
          .first(classOf[User], Option("Id"))
          .get.id
        access.userId = Option(userId)
      }

      if (record.group != null && record.group.length > 0) {
        val groupId: Long = domainModel.query
          .from(classOf[UserGroup])
          .where("name = $", record.group)
          .first(classOf[UserGroup], Option("Id"))
          .get.id
        access.groupId = Option(groupId)
      }
      domainModel.create(access)
      changeTracker.addChange(classOf[Node], nodeId, EntityChangeType.UPDATED, domainModel)
      domainModel.completeTransaction()
      record.accessId = access.id
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def updateRecord(record: NodeAccessModel) {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      val access: NodeAccess = domainModel.query.find(classOf[NodeAccess], record.accessId).get
      access.accessType = record.accessType
      if (record.user != null && record.user.length > 0) {
        val userId: Long = domainModel.query
          .from(classOf[User])
          .where("email = $", record.user)
          .first(classOf[User], None)
          .get.id
        access.userId = Option(userId)
      }
      else {
        access.userId = None
      }
      if (record.group != null && record.group.length > 0) {
        val groupId: Long = domainModel.query
          .from(classOf[UserGroup])
          .where("name = $", record.group)
          .first(classOf[UserGroup], None)
          .get.id
        access.groupId = Option(groupId)
      }
      else {
        access.groupId = None
      }
      domainModel.update(access)
      changeTracker.addChange(classOf[Node], nodeId, EntityChangeType.UPDATED, domainModel)
      domainModel.completeTransaction()
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def deleteRecords(recordIds: Seq[Long]): Boolean = {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      var hasChanges: Boolean = false
      for (recordId <- recordIds) {
        domainModel.delete(classOf[NodeAccess], recordId)
        hasChanges = true
      }
      if (hasChanges) {
        changeTracker.addChange(classOf[Node], nodeId, EntityChangeType.UPDATED, domainModel)
        domainModel.completeTransaction()
      }
      hasChanges
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }
}
