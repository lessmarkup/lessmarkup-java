/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.security

import java.util.OptionalLong

import com.google.gson.JsonObject
import com.lessmarkup.interfaces.data.DomainModel
import scala.collection.JavaConverters._

trait CurrentUser {
  def getUserId: Option[Long]

  @Deprecated
  def getUserIdJava = {
    val userId = getUserId
    if (userId.isDefined) OptionalLong.of(userId.get) else OptionalLong.empty()
  }

  def getGroups: Option[Seq[Long]]

  @Deprecated
  def getGroupsJava = {
    val groups = getGroups
    if (groups.isDefined) groups.get.map(_.asInstanceOf[java.lang.Long]).asJava else null.asInstanceOf
  }

  def getProperties: Option[JsonObject]

  def isAdministrator: Boolean

  def isApproved: Boolean

  def isFakeUser: Boolean

  def emailConfirmed: Boolean

  def getEmail: Option[String]

  def getUserName: Option[String]

  def logout()

  def refresh()

  def loginWithPassword(email: String, password: String, savePassword: Boolean, allowAdmin: Boolean, allowRegular: Boolean, encodedPassword: String): Boolean

  def loginWithOAuth(provider: String, providerUserId: String, savePassword: Boolean, allowAdmin: Boolean, allowRegular: Boolean): Boolean

  def deleteSelf(password: String)

  def checkPassword(domainModel: DomainModel, password: String): Boolean

  def getLoginHash(email: String): (String, String)
}