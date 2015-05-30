/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.configuration

import com.google.gson.{JsonArray, JsonElement, JsonNull, JsonObject}
import com.google.inject.Inject
import com.lessmarkup.framework.helpers.{DependencyResolver, JsonSerializer, LanguageHelper}
import com.lessmarkup.framework.nodehandlers.{AbstractNodeHandler, NodeHandlerConfiguration}
import com.lessmarkup.interfaces.annotations.ConfigurationHandler
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.module.ModuleProvider
import com.lessmarkup.interfaces.recordmodel.RecordModelCache
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory, ChildHandlerSettings}
import com.lessmarkup.userinterface.model.configuration.NodeSettingsModel
import com.lessmarkup.{Constants, TextIds}

object NodeListNodeHandler {

  def getHandlerName(handlerType: Class[_], moduleType: String): String = {
    var typeName: String = handlerType.getName
    if (typeName.endsWith("NodeHandler")) {
      typeName = typeName.substring(0, typeName.length - "NodeHandler".length)
    }
    typeName + " / " + moduleType
  }

  class LayoutInfo(val nodeId: Long, val level: Int)
}

class NodeListNodeHandlerFactory @Inject() (moduleProvider: ModuleProvider, dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new NodeListNodeHandler(moduleProvider, dataCache, nodeHandlerConfiguration)
  }
}

@ConfigurationHandler(titleTextId = TextIds.VIEWS_TREE)
class NodeListNodeHandler(moduleProvider: ModuleProvider, dataCache: DataCache, configuration: NodeHandlerConfiguration) extends AbstractNodeHandler(configuration) {

  override def getScripts: Seq[String] = {
    super.getScripts :+ "scripts/controllers/NodeListController"
  }

  override def getViewData: Option[JsonObject] = {
    val modelCache: RecordModelCache = dataCache.get(classOf[RecordModelCache])
    val node: NodeSettingsModel = DependencyResolver(classOf[NodeSettingsModel])
    val ret: JsonObject = new JsonObject
    val rootNode: NodeSettingsModel = node.getRootNode
    ret.add("root", if (rootNode != null) JsonSerializer.serializePojoToTree(node.getRootNode) else JsonNull.INSTANCE)
    ret.addProperty("nodeSettingsModelId", modelCache.getDefinition(classOf[NodeSettingsModel]).get.getId)
    val handlers: JsonArray = new JsonArray

    for (id <- moduleProvider.getNodeHandlers) {
      val o = new JsonObject
      o.addProperty("id", id)
      val handler = moduleProvider.getNodeHandler(id).get
      o.addProperty("name", NodeListNodeHandler.getHandlerName(handler._1, handler._2))
      handlers.add(o)
    }

    ret.add("nodeHandlers", handlers)
    Option(ret)
  }

  def updateParent(nodeId: Long, parentId: Option[Long], order: Int): JsonElement = {
    val node: NodeSettingsModel = DependencyResolver(classOf[NodeSettingsModel])
    node.nodeId = nodeId
    node.parentId = parentId
    node.position = order
    node.updateParent()
  }

  def createNode(node: NodeSettingsModel): AnyRef = {
    node.createNode
  }

  def deleteNode(id: Long): AnyRef = {
    val node: NodeSettingsModel = DependencyResolver(classOf[NodeSettingsModel])
    node.nodeId = id
    node.deleteNode()
  }

  def updateNode(node: NodeSettingsModel): AnyRef = {
    node.updateNode()
  }

  def changeSettings(nodeId: Long, settings: JsonElement): AnyRef = {
    val node: NodeSettingsModel = DependencyResolver(classOf[NodeSettingsModel])
    node.settings = settings
    node.nodeId = nodeId
    node.changeSettings
  }

  override def hasChildren: Boolean = true

  override def createChildHandler(path: Seq[String]): Option[ChildHandlerSettings] = {
    if (path.length != 2 || "access" != path(1)) {
      return None
    }

    val nodeId = path.head.toLong

    val flatPath = path.mkString("/")

    val nodeHandlerConfiguration = new NodeHandlerConfiguration(
      objectId = Option(nodeId),
      settings = None,
      accessType = getAccessType,
      path = flatPath,
      fullPath = getFullPath + "/" + flatPath
    )

    val handler = createChildHandler(classOf[NodeAccessNodeHandlerFactory], nodeHandlerConfiguration)

    Option(new ChildHandlerSettings(
      handler = handler,
      id = nodeHandlerConfiguration.objectId,
      title = LanguageHelper.getFullTextId(Constants.ModuleTypeMain, TextIds.NODE_ACCESS),
      path = flatPath,
      rest = Nil
    ))
  }
}