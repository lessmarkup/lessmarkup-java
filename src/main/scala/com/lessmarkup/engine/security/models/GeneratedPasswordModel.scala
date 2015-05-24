/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security.models

import com.lessmarkup.interfaces.system.MailTemplateModel

class GeneratedPasswordModel extends MailTemplateModel {

  private var login: String = null
  private var password: String = null
  private var siteName: String = null
  private var siteLink: String = null

  def getLogin: String = login
  def setLogin(login: String): Unit = this.login = login

  def getPassword: String = password
  def setPassword(password: String): Unit = this.password = password

  def getSiteName: String = siteName
  def setSiteName(siteName: String): Unit = this.siteName = siteName

  def getSiteLink: String = siteLink
  def setSiteLink(siteLink: String): Unit = this.siteLink = siteLink
}