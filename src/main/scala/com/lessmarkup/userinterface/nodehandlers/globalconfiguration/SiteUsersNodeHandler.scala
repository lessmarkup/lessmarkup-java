/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.globalconfiguration

import com.google.gson.JsonObject
import com.google.inject.Inject
import com.lessmarkup.TextIds
import com.lessmarkup.framework.helpers.DependencyResolver
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.annotations.ConfigurationHandler
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}
import com.lessmarkup.userinterface.model.global.{UserBlockModel, UserModel}
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler

class SiteUsersNodeHandlerFactory @Inject() (domainModelProvider: DomainModelProvider, dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new SiteUsersNodeHandler(domainModelProvider, dataCache, nodeHandlerConfiguration)
  }
}

@ConfigurationHandler(titleTextId = TextIds.USERS)
class SiteUsersNodeHandler(domainModelProvider: DomainModelProvider, dataCache: DataCache, configuration: NodeHandlerConfiguration)
  extends RecordListNodeHandler[UserModel](domainModelProvider, dataCache, classOf[UserModel], configuration) {

  def block(recordId: Long, newObject: UserBlockModel): JsonObject = {
    newObject.blockUser(recordId)
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val user: UserModel = modelCollection.read(domainModel.query, Seq(recordId)).head
      returnRecordResult(user)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def unblock(recordId: Long): JsonObject = {
    val model: UserBlockModel = DependencyResolver.resolve(classOf[UserBlockModel])
    model.unblockUser(recordId)
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val user: UserModel = modelCollection.read(domainModel.query, Seq(recordId)).head
      returnRecordResult(user)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }
}