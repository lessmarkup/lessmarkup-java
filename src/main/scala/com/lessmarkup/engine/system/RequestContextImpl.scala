/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.system

import java.io.{InputStream, OutputStream}
import javax.servlet.ServletConfig
import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}

import com.lessmarkup.framework.helpers.{DependencyResolver, UrlHelper}
import com.lessmarkup.interfaces.security.CurrentUser
import com.lessmarkup.interfaces.system.{EngineConfiguration, RequestContext}

object RequestContextImpl {
  private val LanguageCookieName: String = "lang"
}

class RequestContextImpl(
                          request: HttpServletRequest,
                          response: HttpServletResponse,
                          servletConfig: ServletConfig,
                          engineConfigurationDefault: Option[EngineConfiguration] = None) extends RequestContext {

  private final val responseCookies: java.util.Map[String, Cookie] = new java.util.HashMap[String, Cookie]
  private lazy val currentUser: CurrentUser = DependencyResolver(classOf[CurrentUser])
  private lazy val engineConfiguration: EngineConfiguration = {
    if (engineConfigurationDefault.isDefined) {
      engineConfigurationDefault.get
    } else {
      new EngineConfigurationImpl(servletConfig)
    }
  }

  def getLanguageId: Option[String] = {
    val languageCookie = getCookie(RequestContextImpl.LanguageCookieName)
    if (languageCookie.isEmpty) {
      None
    } else {
      Option(languageCookie.get.getValue)
    }
  }

  def setLanguageId(languageId: Long) {
    val cookie: Cookie = new Cookie(RequestContextImpl.LanguageCookieName, languageId.asInstanceOf[Long].toString)
    response.addCookie(cookie)
  }

  def getCookie(name: String): Option[Cookie] = {
    val responseCookie: Cookie = responseCookies.get(name)
    if (responseCookie != null) {
      return Option(responseCookie)
    }
    val cookies = request.getCookies
    if (cookies == null) {
      return None
    }
    for (cookie <- cookies) {
      if (cookie.getName == name) {
        return Option(cookie)
      }
    }
    None
  }

  def getPath: String = request.getPathInfo

  def getEngineConfiguration: EngineConfiguration = engineConfiguration

  def redirect(path: String) {
    response.sendRedirect(getBasePath + path)
  }

  def getBasePath: String = UrlHelper.getBaseUrl(request)

  def sendError(errorCode: Int) {
    response.sendError(errorCode)
  }

  def isJsonRequest: Boolean = {
    ("POST" == request.getMethod) && request.getContentType.startsWith("application/json")
  }

  def getCurrentUser: CurrentUser = currentUser

  def getRemoteAddress: String = request.getRemoteAddr

  def setCookie(cookie: Cookie) {
    response.addCookie(cookie)
    responseCookies.put(cookie.getName, cookie)
  }

  def getInputStream: InputStream = request.getInputStream

  def getOutputStream: OutputStream = response.getOutputStream

  def setContentType(contentType: String) {
    response.setContentType(contentType)
  }

  def dispatch(path: String, model: AnyRef) {
    request.setAttribute("model", model)
    request.getRequestDispatcher(path).forward(request, response)
  }

  def addHeader(name: String, value: String) {
    response.addHeader(name, value)
  }

  def mapPath(relativePath: String): String = request.getContextPath + "/" + relativePath

  def getRootPath: String = request.getContextPath
}