/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.common

import com.google.gson.{JsonArray, JsonObject, JsonPrimitive}
import com.lessmarkup.framework.helpers.{JsonSerializer, StringHelper}
import com.lessmarkup.framework.nodehandlers.{AbstractNodeHandler, NodeHandlerConfiguration}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.NodeAccessType
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.structure._
import com.lessmarkup.userinterface.model.structure.LoadNodeViewModel
import com.lessmarkup.userinterface.nodehandlers.common.TabPageNodeHandler.TabPageWithData

object TabPageNodeHandler {
  class TabPage(
      val title: String,
      val handlerType: Class[_ <: NodeHandlerFactory],
      val viewData: Option[JsonObject] = None,
      val viewBody: Option[String] = None,
      val path: String,
      val fullPath: String,
      val uniqueId: Option[String] = None,
      val pageId: Option[Long] = None,
      val settings: String = "",
      val accessType: Option[NodeAccessType] = None)

  protected class TabPageWithData(val configuration: TabPage, val handler: NodeHandler, val viewData: Option[JsonObject], val viewBody: String)
}

abstract class TabPageNodeHandler(dataCache: DataCache, configuration: NodeHandlerConfiguration) extends AbstractNodeHandler(configuration) {

  private val pages = createPages
  private val createdPages = createPageHandlers
  private val scripts = createdPages.flatMap(_.handler.getScripts)

  protected def createPages: Seq[TabPageNodeHandler.TabPage] = {
    val nodeCache: NodeCache = dataCache.get(classOf[NodeCache])

    if (getObjectId.isEmpty) {
      return Nil
    }

    val currentNode: CachedNodeInformation = nodeCache.getNode(getObjectId.get).get

    //val parentPath = currentNode.getFullPath

    val pages = for (
      child <- currentNode.children;
      accessType: NodeAccessType = child.checkRights(RequestContextHolder.getContext.getCurrentUser)
      if accessType != NodeAccessType.NO_ACCESS && child.handlerType.isDefined
    ) yield {
      new TabPageNodeHandler.TabPage(
        handlerType = child.handlerType.get,
        pageId = Option(child.nodeId),
        settings = child.settings,
        path = child.path,
        fullPath = child.fullPath,
        title = child.title,
        accessType = Option(accessType)
      )
    }

    pages.sortBy(_.title)
  }

  private def createPageHandlers: Seq[TabPageNodeHandler.TabPageWithData] = {
    val createdPages: Seq[Option[TabPageWithData]] = for ((page, index) <- pages zipWithIndex) yield {

      val nodeSettings = if (!StringHelper.isNullOrEmpty(page.settings)) {
          Option(JsonSerializer.deserializeToTree(page.settings).getAsJsonObject)
        } else {
          None
        }

      val path = if (StringHelper.isNullOrWhitespace(page.path)) s"page_$index" else page.path

      val nodeHandlerConfiguration = new NodeHandlerConfiguration(
        objectId = Option(index.toLong),
        settings = nodeSettings,
        accessType = page.accessType.getOrElse(getAccessType),
        path = path,
        fullPath = path
      )

      val handler: NodeHandler = createChildHandler(page.handlerType, nodeHandlerConfiguration)

      val viewBody = LoadNodeViewModel.getViewTemplate(handler, dataCache)

      if (viewBody == null) {
        None
      } else {

        val viewData = handler.getViewData

        val pageWithData = new TabPageWithData(
          configuration = page,
          handler = handler,
          viewData = viewData,
          viewBody = viewBody
        )

        Option(pageWithData)
      }
    }

    createdPages.flatten
  }

  protected def createPage[T <: NodeHandlerFactory](handlerType: Class[T], title: String, path: String): TabPageNodeHandler.TabPage = {
    new TabPageNodeHandler.TabPage(
      handlerType = handlerType,
      path = path,
      fullPath = path,
      title = title
    )
  }

  override def getViewData: Option[JsonObject] = {
    val ret: JsonObject = new JsonObject
    val pagesArray: JsonArray = new JsonArray
    createdPages.foreach(p => {
      val o = new JsonObject()
      val c = p.configuration
      o.addProperty("pageId", c.pageId.getOrElse(0L))
      o.addProperty("path", c.path)
      o.addProperty("title", c.title)
      o.addProperty("viewBody", p.viewBody)
      o.add("viewData", p.viewData.orNull)
      o.addProperty("uniqueId", c.uniqueId.get)
      pagesArray.add(o)
    })
    ret.add("pages", pagesArray)
    val requires: JsonArray = new JsonArray
    scripts.foreach(s => requires.add(new JsonPrimitive(s)))
    ret.add("requires", requires)
    Option(ret)
  }

  override def getViewType: String = {
    "TabPage"
  }

  override def hasChildren: Boolean = {
    true
  }

  override def createChildHandler(path: Seq[String]): Option[ChildHandlerSettings] = {
    val pathCombined = path.mkString("/")
    val page = createdPages.find(_.configuration.path == pathCombined)
    if (page.isEmpty) {
      return None
    }
    val nodeSettings = if (!StringHelper.isNullOrEmpty(page.get.configuration.settings)) {
      Option(JsonSerializer.deserializeToTree(page.get.configuration.settings).getAsJsonObject)
    } else {
      None
    }

    val nodeHandlerConfiguration = new NodeHandlerConfiguration(
      objectId = page.get.configuration.pageId,
      settings = nodeSettings,
      accessType = page.get.configuration.accessType.getOrElse(getAccessType),
      path = page.get.configuration.path,
      fullPath = page.get.configuration.fullPath
    )

    val handler: NodeHandler = createChildHandler(page.get.configuration.handlerType, nodeHandlerConfiguration)

    Option(new ChildHandlerSettings(
      handler = handler,
      id = page.get.configuration.pageId,
      title = page.get.configuration.title,
      path = page.get.configuration.path,
      rest = Seq.empty
    ))
  }
}
