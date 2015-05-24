/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security.models

import com.lessmarkup.interfaces.system.MailTemplateModel

class NewUserCreatedModel extends MailTemplateModel {

  private var userId: Long = 0L
  private var name: String = null
  private var email: String = null
  private var password: String = null
  private var siteName: String = null

  def getUserId: Long = userId
  def setUserId(userId: Long): Unit = this.userId = userId

  def getName: String = name
  def setName(name: String): Unit = this.name = name

  def getEmail: String = email
  def setEmail(email: String): Unit = this.email = email

  def getPassword: String = password
  def setPassword(password: String): Unit = this.password = password

  def getSiteName: String = siteName
  def setSiteName(siteName: String): Unit = this.siteName = siteName
}
