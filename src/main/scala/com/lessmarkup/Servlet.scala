/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup

import java.io.IOException
import java.util.regex.{Matcher, Pattern}
import javax.servlet.ServletException
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.lessmarkup.engine.data.migrate.MigrateEngine
import com.lessmarkup.engine.module.ModuleProviderImpl
import com.lessmarkup.engine.system.RequestContextImpl
import com.lessmarkup.framework.helpers.{DependencyResolver, LoggingHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.data.{ChangeTracker, DomainModelProvider}
import com.lessmarkup.interfaces.module.{ModuleActionHandler, ModuleIntegration, ModuleProvider}
import com.lessmarkup.interfaces.system.RequestContext
import com.lessmarkup.userinterface.model.structure.{JsonEntryPointModel, NodeEntryPointModel, ResourceModel}

object Servlet {
  private val languagePattern: Pattern = Pattern.compile("language/([0-9]+)")
}

class Servlet extends HttpServlet {

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  override def getServletInfo: String = "LessMarkup"

  @throws(classOf[ServletException])
  override def init() {
    super.init()
    LoggingHelper.getLogger(getClass).info("Initializing application")
    val requestContext: RequestContextImpl = new RequestContextImpl(null, null, getServletConfig)
    RequestContextHolder.onRequestStarted(requestContext)
    try {
      val moduleProvider: ModuleProvider = new ModuleProviderImpl()
      val migrateEngine: MigrateEngine = DependencyResolver(classOf[MigrateEngine])
      migrateEngine.execute()
      moduleProvider.updateModuleDatabase(DependencyResolver(classOf[DomainModelProvider]))
    } finally {
      RequestContextHolder.onRequestFinished()
    }
    LoggingHelper.getLogger(getClass).info("Successfully initialized application")
  }

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    LoggingHelper.getLogger(getClass).info("Start of doGet")
    processRequest(request, response)
    LoggingHelper.getLogger(getClass).info("End of doGet")
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    LoggingHelper.getLogger(getClass).info("Start of doPost")
    processRequest(request, response)
    LoggingHelper.getLogger(getClass).info("End of doPost")
  }

  protected def processRequest(request: HttpServletRequest, response: HttpServletResponse) {
    var path: String = request.getPathInfo
    if (path.startsWith("/")) {
      path = path.substring(1)
    }
    val requestContext: RequestContextImpl = new RequestContextImpl(request, response, getServletConfig)
    RequestContextHolder.onRequestStarted(requestContext)
    try {
      DependencyResolver(classOf[ChangeTracker]).enqueueUpdates()
      if (!processRequestInContext(path, requestContext)) {
        response.sendError(404)
      }
    } finally {
      RequestContextHolder.onRequestFinished()
    }
  }

  private def processRequestInContext(path: String, requestContext: RequestContext): Boolean = {
    LoggingHelper.getLogger(getClass).info(String.format("Request to '%s'", path))
    if (path.startsWith(Constants.NodePathSystemAction + "/")) {
      var actionPath: String = path.substring(Constants.NodePathSystemAction.length + 1)
      val pos: Int = actionPath.indexOf('/')
      var name: String = null
      if (pos <= 0) {
        name = actionPath
        actionPath = null
      }
      else {
        name = actionPath.substring(0, pos)
        actionPath = actionPath.substring(pos + 1)
      }
      val action: ModuleActionHandler = DependencyResolver(classOf[ModuleIntegration]).getActionHandler(name)
      if (action != null) {
        action.handleAction(actionPath)
        return true
      }
    }
    val matcher: Matcher = Servlet.languagePattern.matcher(path)
    if (matcher.matches && matcher.groupCount == 1) {
      LoggingHelper.getLogger(getClass).info("Handling language change request")
      try {
        val languageId = matcher.group(0).toLong
        requestContext.setLanguageId(languageId)
        requestContext.redirect("/")
        return true
      }
      catch {
        case e: NumberFormatException =>
          LoggingHelper.getLogger(getClass).warning("Unknown language id")
          requestContext.sendError(400)
          return true
      }
    }

    if (JsonEntryPointModel.appliesToRequest) {
      LoggingHelper.getLogger(getClass).info("Start of JSON request")
      val model: JsonEntryPointModel = DependencyResolver(classOf[JsonEntryPointModel])
      model.handleRequest()
      LoggingHelper.getLogger(getClass).info("End of JSON request")
      return true
    }

    val nodeModel: NodeEntryPointModel = DependencyResolver(classOf[NodeEntryPointModel])
    if (nodeModel.initialize(path)) {
      nodeModel.handleRequest()
      return true
    }

    val resourceModel: ResourceModel = DependencyResolver(classOf[ResourceModel])
    if (resourceModel.initialize(path)) {
      LoggingHelper.getLogger(getClass).info("Handling resource access request")
      resourceModel.handleRequest()
      LoggingHelper.getLogger(getClass).info("Finished handling resource access request")
      return true
    }

    false
  }
}