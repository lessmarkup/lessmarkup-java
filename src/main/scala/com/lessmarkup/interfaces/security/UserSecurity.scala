/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.security

import java.time.OffsetDateTime

trait UserSecurity {
  def changePassword(password: String): (String, String)

  def createUser(username: String, password: String, email: String, preApproved: Boolean, generatePassword: Boolean): Long

  def createPasswordChangeToken(userId: Long): Option[String]

  def validatePasswordChangeToken(email: String, token: String): Option[Long]

  def createAccessToken(collectionId: Int, entityId: Long, accessType: Int, userId: Option[Long], expirationTime: Option[OffsetDateTime]): String

  def validateAccessToken(token: String, collectionId: Int, entityId: Long, accessType: Int, userId: Option[Long]): Boolean

  def generateUniqueId: String

  def confirmUser(validateSecret: String): Option[Long]

  def encryptObject(obj: AnyRef): Option[String]

  def decryptObject[T](objectType: Class[T], encrypted: String): Option[T]

  def encryptLoginTicket(ticket: LoginTicket): String

  def decryptLoginTicket(ticket: String): Option[LoginTicket]
}
