/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.system

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.NodeAccessType
import com.lessmarkup.interfaces.cache.CacheHandler
import com.lessmarkup.interfaces.structure.CachedNodeInformation

trait UserCache extends CacheHandler {
  def getName: String

  def isRemoved: Boolean

  def isAdministrator: Boolean

  def isApproved: Boolean

  def isEmailConfirmed: Boolean

  def getGroups: Seq[Long]

  def getEmail: String

  def getTitle: String

  def isBlocked: Boolean

  def getUnblockTime: Option[OffsetDateTime]

  def getProperties: String

  def getAvatarImageId: Option[Long]

  def getUserImageId: Option[Long]

  def getNodes: Seq[(CachedNodeInformation, NodeAccessType)]
}