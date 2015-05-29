/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.common

import java.lang.reflect.Method

import com.google.gson._
import com.lessmarkup.framework.helpers.{JsonSerializer, StringHelper}
import com.lessmarkup.framework.nodehandlers.{AbstractNodeHandler, NodeHandlerConfiguration}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.NodeAccessType
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.structure._
import com.lessmarkup.userinterface.model.common.{FlatPagePosition, FlatPageSettingsModel}
import com.lessmarkup.userinterface.model.structure.LoadNodeViewModel

abstract class FlatPageNodeHandler(dataCache: DataCache, configuration: NodeHandlerConfiguration) extends AbstractNodeHandler(configuration) {

  private class FlatNodeEntry(val title: String,
                              val viewData: Option[JsonObject],
                              val viewBody: String,
                              val handlerType: Class[_ <: NodeHandlerFactory],
                              val nodeId: Long,
                              val settings: String,
                              val anchor: String,
                              val uniqueId: String,
                              val level: Int,
                              val path: String,
                              val fullPath: String,
                              val accessType: NodeAccessType,
                              val source: CachedNodeInformation,
                              val handler: NodeHandler)

  private class TreeNodeEntry(val anchor: String, val title: String, val flatEntry: FlatNodeEntry, val children: Seq[TreeNodeEntry])

  private val treeRoot: Option[TreeNodeEntry] = createRootNode
  private val flatNodeList: Seq[FlatNodeEntry] = if (treeRoot.isDefined) flattenTree(treeRoot.get) else Seq()
  private val scripts: Seq[String] = flatNodeList.flatMap(n => n.handler.getScripts)
  private val stylesheets: Seq[String] = flatNodeList.flatMap(n => n.handler.getStylesheets)

  private def flattenTree(treeNodeEntry: TreeNodeEntry): Seq[FlatNodeEntry] = {
    treeNodeEntry.children.flatMap(flattenTree) :+ treeNodeEntry.flatEntry
  }

  private def createRootNode: Option[TreeNodeEntry] = {
    if (getObjectId.isEmpty) {
      return None
    }

    val nodeCache: NodeCache = dataCache.get(classOf[NodeCache])

    val node = nodeCache.getNode(getObjectId.get)

    if (node.isEmpty) {
      return None
    }

    val settingsModel: Option[FlatPageSettingsModel] = getSettings(classOf[FlatPageSettingsModel])

    val levelToLoad = if (settingsModel != null && settingsModel.get.levelToLoad != 0) settingsModel.get.levelToLoad else 2

    createNode(node.get, "", 0, levelToLoad)
  }

  private def createNode(source: CachedNodeInformation, anchor: String, level: Int, maxLevel: Int): Option[TreeNodeEntry] = {

    val accessType = source.checkRights(RequestContextHolder.getContext.getCurrentUser)

    if (accessType == NodeAccessType.NO_ACCESS) {
      return None
    }

    val nodeSettings: Option[JsonObject] = if (!StringHelper.isNullOrEmpty(source.settings)) {
      Option(JsonSerializer.deserializeToTree(source.settings).getAsJsonObject)
    } else {
      None
    }

    val childHandlerSettings = new NodeHandlerConfiguration(Option(source.nodeId), nodeSettings, accessType, source.path, source.fullPath)
    val childHandler = createChildHandler(source.handlerType.get, childHandlerSettings)

    val viewBody = LoadNodeViewModel.getViewTemplate(childHandler, dataCache)

    if (viewBody == null) {
      return None
    }

    val entry: FlatNodeEntry = new FlatNodeEntry(
      title = source.title,
      viewData = childHandler.getViewData,
      viewBody = viewBody,
      handlerType = source.handlerType.get,
      nodeId = source.nodeId,
      settings = source.settings,
      anchor = anchor,
      uniqueId = s"flatpage${source.nodeId}",
      level = level,
      path = source.fullPath,
      fullPath = source.fullPath,
      accessType = accessType,
      source = source,
      handler = childHandler
    )

    val children = if (level > maxLevel) Seq() else source.children
      .filter(c => c.handlerType != null && c.handlerType.get != classOf[FlatPageNodeHandler] && c.enabled)
      .flatMap(c => {
      val childAnchor = if (StringHelper.isNullOrEmpty(anchor)) "" else anchor + "_"
      createNode(c, childAnchor, level+1, maxLevel)
    })

    Option(new TreeNodeEntry(anchor, source.title, entry, children))
  }

  override def getViewData: Option[JsonObject] = {

    val settingsModel = getSettings(classOf[FlatPageSettingsModel])
    val ret = new JsonObject
    val children = new JsonArray
    if (treeRoot.isDefined) {
      for (child <- treeRoot.get.children) {
        children.add(JsonSerializer.serializePojoToTree(child))
      }
    }
    ret.add("tree", children)
    val flatArray: JsonArray = new JsonArray
    for (entry <- flatNodeList) {
      val o = new JsonObject
      o.addProperty("anchor", entry.anchor)
      o.addProperty("level", entry.level)
      o.addProperty("nodeId", entry.nodeId)
      o.addProperty("path", entry.path)
      o.addProperty("title", entry.title)
      o.addProperty("uniqueId", entry.uniqueId)
      o.addProperty("viewBody", entry.viewBody)
      if (entry.viewData.isDefined) {
        o.add("viewData", entry.viewData.get)
      } else {
        o.add("viewData", JsonNull.INSTANCE)
      }
      flatArray.add(o)
    }
    ret.add("flat", flatArray)
    ret.addProperty("position", if (settingsModel.isDefined) settingsModel.get.position.toString else FlatPagePosition.RIGHT.toString)
    val scriptsArray: JsonArray = new JsonArray
    for(s <- scripts) {
      scriptsArray.add(new JsonPrimitive(s))
    }
    ret.add("scripts", scriptsArray)
    val stylesheetsArray = new JsonArray
    for (s <- stylesheets) {
      stylesheetsArray.add(new JsonPrimitive(s))
    }
    ret.add("stylesheets", stylesheetsArray)
    Option(ret)
  }

  override def getSettingsModel: Option[Class[_]] = {
    Option(classOf[FlatPageSettingsModel])
  }

  override def hasChildren: Boolean = {
    true
  }

  private def constructHandler(node: FlatNodeEntry): NodeHandler = {
    val accessType: NodeAccessType = node.source.checkRights(RequestContextHolder.getContext.getCurrentUser)
    if (accessType eq com.lessmarkup.interfaces.annotations.NodeAccessType.NO_ACCESS) {
      return null
    }

    val nodeSettings =
      if (!StringHelper.isNullOrEmpty(node.settings)) {
        Option(JsonSerializer.deserializeToTree(node.settings).getAsJsonObject)
      } else {
        None
      }

    val configuration = new NodeHandlerConfiguration(Option(node.nodeId), nodeSettings, node.accessType, node.path, node.fullPath)

    createChildHandler(node.handlerType, configuration)
  }

  def getChildHandler(path: String): ChildHandlerSettings = {
    val node = flatNodeList.find(n => n.path == path)
    if (node.isEmpty) {
      return null
    }
    val handler: NodeHandler = constructHandler(node.get)
    new ChildHandlerSettings(handler, Option(node.get.nodeId), node.get.title, node.get.path, Seq())
  }

  override def getActionHandler(name: String, data: JsonObject): Option[(AnyRef, Method)] = {
    val nodeIdElement: JsonElement = data.get("flatNodeId")
    if (nodeIdElement != null) {
      val nodeId: Long = nodeIdElement.getAsLong
      val node = flatNodeList.find(n => n.nodeId == nodeId)
      if (node.isDefined) {
        val handler: NodeHandler = constructHandler(node.get)
        return handler.getActionHandler(name, data)
      }
    }
    super.getActionHandler(name, data)
  }
}