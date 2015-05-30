/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

import java.util.logging.{Level, Logger}

import com.google.gson.{JsonArray, JsonObject}
import com.google.inject.Inject
import com.lessmarkup.framework.helpers.{DependencyResolver, JsonSerializer}
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.NodeAccessType
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.structure._
import com.lessmarkup.interfaces.system.SiteConfiguration

class UserInterfaceElementsModel @Inject() (domainModelProvider: DomainModelProvider, dataCache: DataCache) {

  def handle(serverConfiguration: JsonObject, lastChangeId: Option[Long]) {
    val notifications: JsonArray = new JsonArray
    val nodeCache: NodeCache = dataCache.get(classOf[NodeCache])
    val siteConfiguration: SiteConfiguration = dataCache.get(classOf[SiteConfiguration])
    val domainModel: DomainModel = this.domainModelProvider.create

    try {
      for (nodeInfo <- nodeCache.getNodes
           if nodeInfo.handlerType.isDefined && classOf[NotificationProviderFactory].isAssignableFrom(nodeInfo.handlerType.get)
           if nodeInfo.checkRights(RequestContextHolder.getContext.getCurrentUser) != NodeAccessType.NO_ACCESS) {
        val handlerFactory = DependencyResolver(nodeInfo.handlerType.get)
        val settings = if (nodeInfo.settings != null && nodeInfo.settings.length > 0) {
          Option(JsonSerializer.deserializeToTree(nodeInfo.settings).getAsJsonObject)
        } else {
          None
        }

        val accessType = nodeInfo.checkRights(RequestContextHolder.getContext.getCurrentUser)

        val configuration = new NodeHandlerConfiguration(
          objectId = Option(nodeInfo.nodeId),
          settings = settings,
          accessType = accessType,
          path = nodeInfo.path,
          fullPath = nodeInfo.fullPath
        )

        val node = handlerFactory.createNodeHandler(configuration)
        val notificationProvider: NotificationProvider = node.asInstanceOf[NotificationProvider]
        val notification: JsonObject = new JsonObject
        notification.addProperty("id", nodeInfo.nodeId)
        notification.addProperty("title", notificationProvider.getTitle)
        notification.addProperty("tooltip", notificationProvider.getTooltip)
        notification.addProperty("icon", notificationProvider.getIcon)
        notification.addProperty("path", nodeInfo.fullPath)
        notification.addProperty("count", notificationProvider.getValueChange(None, lastChangeId, domainModel))
        notifications.add(notification)
      }
    }
    catch {
      case ex: Exception =>
        Logger.getLogger(classOf[UserInterfaceElementsModel].getName).log(Level.SEVERE, null, ex)
    } finally {
      if (domainModel != null) domainModel.close()
    }

    val menuNodes: JsonArray = new JsonArray
    nodeCache.getNodes.filter(n => n.addToMenu && n.enabled).foreach(n => {
      val o: JsonObject = new JsonObject()
      o.addProperty("title", n.title)
      o.addProperty("url", n.fullPath)
      menuNodes.add(o)
    })
    serverConfiguration.add("topMenu", menuNodes)
    serverConfiguration.add("collections", notifications)
    if (siteConfiguration.hasNavigationBar) {
      val navigationTree: JsonArray = new JsonArray
      fillNavigationBarItems(nodeCache.getRootNode.get.children, 0, menuNodes)
      serverConfiguration.add("navigationTree", navigationTree)
    }
  }

  private def fillNavigationBarItems(nodes: Seq[CachedNodeInformation], level: Int, menuItems: JsonArray) {
    nodes
      .filter(_.enabled)
      .foreach(node => {

      val accessType = node.checkRights(RequestContextHolder.getContext.getCurrentUser)

      if (accessType != NodeAccessType.NO_ACCESS) {
        val model = new JsonObject()

        model.addProperty("title", node.title)
        model.addProperty("url", node.fullPath)
        model.addProperty("level", level)

        menuItems.add(model)

        fillNavigationBarItems(node.children, level + 1, menuItems)
      }
    })
  }
}
