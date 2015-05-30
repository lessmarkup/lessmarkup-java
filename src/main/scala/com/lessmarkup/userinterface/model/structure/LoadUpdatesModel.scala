/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

import com.google.gson.{JsonArray, JsonObject}
import com.google.inject.Inject
import com.lessmarkup.framework.helpers.{DependencyResolver, JsonSerializer}
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.structure._
import com.lessmarkup.interfaces.system.UserCache

class LoadUpdatesModel @Inject() (domainModelProvider: DomainModelProvider, dataCache: DataCache) {

  def handle(versionId: Option[Long], newVersionId: Option[Long], path: String, arguments: JsonObject, returnValues: JsonObject, currentNodeId: Option[Long]) {
    val userCache: UserCache = dataCache.get(classOf[UserCache])
    val nodeCache: NodeCache = dataCache.get(classOf[NodeCache])
    if ((newVersionId.isEmpty || newVersionId == versionId) && currentNodeId.isEmpty) {
      return
    }
    var currentProvider: NotificationProvider = null
    val handlers = userCache.getNodes.filter(_._1.handlerType.get.isAssignableFrom(classOf[NotificationProvider])).flatMap {
      case (node, access) =>
        val handlerFactory: NodeHandlerFactory = DependencyResolver(node.handlerType.get)
        val settings = if (node.settings != null && node.settings.length > 0) {
          Option(JsonSerializer.deserializeToTree(node.settings).getAsJsonObject)
        } else {
          None
        }
        val nodeHandlerConfiguration = new NodeHandlerConfiguration(
          objectId = Option(node.nodeId),
          settings = settings,
          accessType = access,
          path = node.path,
          fullPath = node.fullPath
        )
        val handler = handlerFactory.createNodeHandler(nodeHandlerConfiguration)
        val notificationProvider: NotificationProvider = handler.asInstanceOf[NotificationProvider]
        if (notificationProvider == null) {
          None
        } else {
          if (currentNodeId.isDefined && currentProvider == null && node.nodeId == currentNodeId.get) {
            currentProvider = notificationProvider
          }
          Option((node.nodeId, notificationProvider))
        }
    }
    if ((newVersionId.isEmpty || newVersionId == versionId) && currentProvider == null) {
      return
    }
    val notificationChanges: JsonArray = new JsonArray
    val domainModel: DomainModel = domainModelProvider.create
    try {
      for (handler <- handlers if handler._2 != null) {
        if (handler._2 == currentProvider) {
          val change: Int = handler._2.getValueChange(null, newVersionId, domainModel)
          val c: JsonObject = new JsonObject
          c.addProperty("id", handler._1)
          c.addProperty("newValue", change)
          notificationChanges.add(c)
        }
        else {
          val change: Int = handler._2.getValueChange(versionId, newVersionId, domainModel)
          if (change > 0) {
            val c: JsonObject = new JsonObject
            c.addProperty("id", handler._1)
            c.addProperty("change", change)
            notificationChanges.add(c)
          }
        }
      }
      val currentHandler = nodeCache.getNodeHandler(path)
      if (currentHandler.isDefined && newVersionId.isDefined) {
        val updates: JsonObject = new JsonObject
        currentHandler.get.processUpdates(versionId, newVersionId.get, updates, domainModel, arguments)
        if (updates.entrySet.size > 0) {
          returnValues.add("updates", updates)
        }
      }
    } finally {
      if (domainModel != null) domainModel.close()
    }
    if (notificationChanges.size > 0) {
      returnValues.add("notificationChanges", notificationChanges)
    }
  }
}