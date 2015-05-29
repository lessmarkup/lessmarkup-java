/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.configuration

import com.google.gson.{JsonArray, JsonObject}
import com.google.inject.Inject
import com.lessmarkup.framework.helpers.LanguageHelper
import com.lessmarkup.framework.nodehandlers.{AbstractNodeHandler, NodeHandlerConfiguration}
import com.lessmarkup.interfaces.annotations._
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.module.ModuleProvider
import com.lessmarkup.interfaces.structure._

class ConfigurationGroupData(val title: String, val handlers: Seq[ConfigurationHandlerData])

class ConfigurationHandlerData(
  val handlerType: Class[_ <: NodeHandlerFactory],
  val titleTextId: String,
  val moduleType: String,
  val groupTextId: String,
  val title: String,
  val id: Long,
  val typeName: String)

class ConfigurationRootNodeHandlerFactory @Inject() (moduleProvider: ModuleProvider, dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new ConfigurationRootNodeHandler(moduleProvider, dataCache, nodeHandlerConfiguration)
  }
}

class ConfigurationRootNodeHandler(moduleProvider: ModuleProvider, dataCache: DataCache, configuration: NodeHandlerConfiguration)
  extends AbstractNodeHandler(configuration) {

  private val allHandlers: Seq[ConfigurationHandlerData] = {
    (for (
      ((module, handlerType), index) <- moduleProvider.getModules
        .flatMap(m => m.getInitializer.getNodeHandlerTypes.map(t => (m, t)))
        .iterator.zipWithIndex;
      configurationHandler = handlerType.getAnnotation(classOf[ConfigurationHandler])
      if configurationHandler != null
    ) yield {

        val fullTypeName = handlerType.getSimpleName.toLowerCase
        val minimizedTypeName =
          if (fullTypeName.endsWith("nodehandler"))
            fullTypeName.substring(0, fullTypeName.length - "nodehandler".length)
          else
            fullTypeName

        new ConfigurationHandlerData(
          handlerType = handlerType,
          moduleType = module.getModuleType,
          titleTextId = configurationHandler.titleTextId,
          title = LanguageHelper.getFullTextId(module.getModuleType, configurationHandler.titleTextId),
          id = index,
          typeName = minimizedTypeName,
          groupTextId = configurationHandler.groupTextId()
        )
      }).toSeq
  }

  private val configurationGroups: Seq[ConfigurationGroupData] = allHandlers.groupBy(_.groupTextId).map {
      case (groupTextId, handlers) =>
        new ConfigurationGroupData(groupTextId, handlers.sortBy(_.title))
    }.toSeq

  private final val configurationHandlers: Map[String, ConfigurationHandlerData] = allHandlers.map(h => (h.typeName, h)).toMap

  override def getScripts: Seq[String] = super.getScripts :+ "scripts/controllers/ConfigurationController"

  override def hasChildren: Boolean = true

  override def getViewData: Option[JsonObject] = {
    val path: String = if (getObjectId.isDefined) dataCache.get(classOf[NodeCache]).getNode(getObjectId.get).get.fullPath else null
    val ret: JsonObject = new JsonObject
    val groups: JsonArray = new JsonArray
    for (g <- this.configurationGroups) {
      val group: JsonObject = new JsonObject
      group.addProperty("title", g.title)
      val items: JsonArray = new JsonArray
      for (h <- g.handlers) {
        val handler = new JsonObject
        handler.addProperty("path", if (path != null) path + "/" + h.typeName else h.typeName)
        handler.addProperty("title", LanguageHelper.getFullTextId(h.moduleType, h.titleTextId))
        items.add(handler)
      }

      group.add("items", items)
      groups.add(group)
    }
    ret.add("groups", groups)
    Option(ret)
  }

  override def createChildHandler(path: Seq[String]): Option[ChildHandlerSettings] = {

    if (path.isEmpty) {
      return None
    }

    val handlerData = configurationHandlers.get(path.head)
    if (handlerData.isEmpty) {
      return None
    }

    val nodeHandlerConfiguration = new NodeHandlerConfiguration(
      objectId = Option(handlerData.get.id),
      settings = None,
      accessType = NodeAccessType.WRITE,
      path = path.head,
      fullPath = getFullPath + "/" + path.head
    )

    val handler = createChildHandler(handlerData.get.handlerType, nodeHandlerConfiguration)

    Option(new ChildHandlerSettings(
      handler = handler,
      id = nodeHandlerConfiguration.objectId,
      title = handlerData.get.title,
      path = path.head,
      rest = path.tail
    ))
  }
}