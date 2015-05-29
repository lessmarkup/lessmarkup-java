/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.common

import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.{DataCache, EntityChangeType}
import com.lessmarkup.interfaces.data.{ChangesCache, DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.recordmodel.{ModelCollection, RecordModel}
import com.lessmarkup.interfaces.structure.NotificationProvider

abstract class RecordListWithNotifyNodeHandler[T <: RecordModel[_]](
    domainModelProvider: DomainModelProvider,
    dataCache: DataCache,
    modelType: Class[T],
    configuration: NodeHandlerConfiguration
  )
  extends RecordListNodeHandler[T](domainModelProvider, dataCache, modelType, configuration) with NotificationProvider {

  def getTitle: String
  def getTooltip: String
  def getIcon: String

  def getValueChange(fromVersion: Option[Long], toVersion: Option[Long], domainModel: DomainModel): Int = {

    val changesCache = dataCache.get(classOf[ChangesCache])
    val userId = RequestContextHolder.getContext.getCurrentUser.getUserId
    val collection: ModelCollection[T] = modelCollection
    val changes = changesCache.getCollectionChanges(collection.getCollectionId, fromVersion, toVersion, Option(
      change => !(userId.isDefined && change.getUserId.isDefined && change.getUserId.get == userId.get)
      && change.getType != EntityChangeType.REMOVED))

    if (changes.isEmpty) {
      return 0
    }

    val changeIds = changes.map(c => c.getEntityId)

    collection.readIds(domainModel.query.whereIds(changeIds), ignoreOrder = true).size
  }

  protected override def isSupportsLiveUpdates: Boolean = false

  protected override def isSupportsManualRefresh: Boolean = false
}
