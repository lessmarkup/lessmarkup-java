/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

import java.net.URLDecoder

import com.google.gson.{JsonArray, JsonNull, JsonObject, JsonPrimitive}
import com.google.inject.Inject
import com.lessmarkup.framework.helpers.StringHelper
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.structure.{NodeCache, NodeHandler}
import com.lessmarkup.interfaces.system.ResourceCache

import scala.collection.mutable

object LoadNodeViewModel {
  def getViewTemplate(handler: NodeHandler, dataCache: DataCache): String = {
    val viewPath: String = getViewPath(handler.getViewType)
    val resourceCache: ResourceCache = dataCache.get(classOf[ResourceCache])
    var template: String = resourceCache.parseText(viewPath + ".html").get
    val stylesheets: Seq[String] = handler.getStylesheets
    if (stylesheets != null && stylesheets.nonEmpty) {
      val sb: StringBuilder = new StringBuilder
      sb.append("<style scoped=\"scoped\">")
      stylesheets.foreach(stylesheet => {
        sb.append(resourceCache.readText(stylesheet + ".css"))
      })
      sb.append("</style>")
      template = sb.toString + template
    }
    template
  }

  def getViewPath(viewName: String): String = {
    val parsedViewName = if (viewName.endsWith("NodeHandler")) {
      viewName.substring(0, viewName.length - "NodeHandler".length)
    } else {
      viewName
    }

    "views/" + StringHelper.toJsonCase(parsedViewName)
  }
}

class LoadNodeViewModel @Inject() (dataCache: DataCache) {
  private var nodeHandler: NodeHandler = null
  private var nodeId: Option[Long] = None
  private var breadcrumbs: mutable.ArrayBuffer[NodeBreadcrumbModel] = mutable.ArrayBuffer()
  private var toolbarButtons: List[ToolbarButtonModel] = null
  private var title: String = null
  private val path: String = null
  private var templateId: String = null
  private var template: String = null
  private var viewData: JsonObject = null
  private var isStatic: Boolean = false
  private var require: Seq[String] = null

  def getNodeHandler: NodeHandler = nodeHandler
  def getNodeId: Option[Long] = nodeId

  def initialize(path: String, cachedTemplates: Seq[String], initializeUiElements: Boolean, tryCreateResult: Boolean): Boolean = {

    var decodedPath = URLDecoder.decode(path, "UTF-8")

    decodedPath = if (decodedPath == null) "" else {
      val queryPost: Int = decodedPath.indexOf('?')
      if (queryPost >= 0) {
        decodedPath.substring(0, queryPost)
      } else {
        decodedPath
      }
    }

    val nodeCache = dataCache.get(classOf[NodeCache])

    val handlerFilter = (nodeHandler: NodeHandler, nodeTitle: String, nodePath: String, rest: Seq[String], nodeIdLocal: Option[Long]) => {
      if (nodeIdLocal.isDefined) {
        nodeId = nodeId
      }

      if (initializeUiElements) {
        breadcrumbs += new NodeBreadcrumbModel(nodeTitle, nodePath)
      }

      title = nodeTitle
      decodedPath = nodePath

      false
    }

    nodeHandler = nodeCache.getNodeHandler(decodedPath, Option(handlerFilter)).get

    if (initializeUiElements && this.breadcrumbs.nonEmpty) {
      breadcrumbs -= breadcrumbs.last
    }

    if (nodeHandler == null) {
      return false
    }

    this.templateId = this.nodeHandler.getTemplateId
    if (initializeUiElements) {
      this.toolbarButtons = List()
      if (cachedTemplates == null || !cachedTemplates.exists(_.equals(this.templateId))) {
        this.template = LoadNodeViewModel.getViewTemplate(this.nodeHandler, this.dataCache)
        if (this.template == null) {
          return false
        }
      }
      this.viewData = nodeHandler.getViewData.get
    }
    this.isStatic = nodeHandler.isStatic
    this.require = nodeHandler.getScripts
    true
  }

  def toJson: JsonObject = {
    val ret: JsonObject = new JsonObject
    ret.addProperty("template", this.template)
    ret.addProperty("templateId", this.templateId)
    ret.addProperty("title", this.title)
    ret.add("viewData", this.viewData)
    ret.addProperty("isStatic", this.isStatic)
    ret.addProperty("path", this.path)
    val array: JsonArray = new JsonArray
    if (this.require != null) {
      for (script <- this.require) {
        array.add(new JsonPrimitive(script))
      }
    }
    ret.add("require", array)
    if (this.nodeId.isDefined) {
      ret.addProperty("nodeId", this.nodeId.get)
    }
    else {
      ret.add("nodeId", JsonNull.INSTANCE)
    }
    val arrayBreadcrumbs: JsonArray = new JsonArray
    if (this.breadcrumbs != null) {
      this.breadcrumbs.foreach(b => {
        val b1 = new JsonObject()
        b1.addProperty("text", b.getText)
        b1.addProperty("url", b.getUrl)
        arrayBreadcrumbs.add(b1)
      })
    }
    ret.add("breadcrumbs", arrayBreadcrumbs)
    val arrayButtons: JsonArray = new JsonArray
    if (this.toolbarButtons != null) {
      for (b <- toolbarButtons) {
        val b1: JsonObject = new JsonObject
        b1.addProperty("id", b.getId)
        b1.addProperty("text", b.getText)
        arrayButtons.add(b1)
      }
    }
    ret.add("toolbarButtons", arrayButtons)
    ret
  }
}