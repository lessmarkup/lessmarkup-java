/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.system

import java.io.{InputStream, OutputStream}
import javax.servlet.http.Cookie

import com.lessmarkup.interfaces.security.CurrentUser

trait RequestContext {
  def getLanguageId: Option[String]

  def setLanguageId(languageId: Long)

  def getBasePath: String

  def getPath: String

  def getRootPath: String

  def getEngineConfiguration: EngineConfiguration

  def redirect(path: String)

  def sendError(errorCode: Int)

  def isJsonRequest: Boolean

  def getCurrentUser: CurrentUser

  def getRemoteAddress: String

  def getCookie(name: String): Option[Cookie]

  def setCookie(cookie: Cookie)

  def getInputStream: InputStream

  def getOutputStream: OutputStream

  def setContentType(contentType: String)

  def dispatch(path: String, model: AnyRef)

  def addHeader(name: String, value: String)

  def mapPath(relativePath: String): String
}