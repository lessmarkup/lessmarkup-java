/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.structure

import com.lessmarkup.interfaces.annotations
import com.lessmarkup.interfaces.annotations.NodeAccessType
import com.lessmarkup.interfaces.security.CurrentUser
import com.lessmarkup.interfaces.system.UserCache

trait CachedNodeInformation {
  val nodeId: Long
  val enabled: Boolean
  val path: String
  val order: Int
  val level: Int
  val title: String
  val description: String
  val handlerId: String
  val parentNodeId: Option[Long]
  var parent: Option[CachedNodeInformation]
  val accessList: Seq[CachedNodeAccess]
  val children: Seq[CachedNodeInformation]
  val fullPath: String
  val handlerType: Option[Class[_ <: NodeHandlerFactory]]
  val handlerModuleType: Option[String]
  val settings: String
  var root: CachedNodeInformation
  val addToMenu: Boolean
  val loggedIn: Boolean

  def checkRights(currentUser: CurrentUser, defaultAccessType: annotations.NodeAccessType): annotations.NodeAccessType

  def checkRights(currentUser: CurrentUser): annotations.NodeAccessType = {
    checkRights(currentUser, NodeAccessType.READ)
  }

  def checkRights(userCache: UserCache, userId: Option[Long], defaultAccessType: annotations.NodeAccessType): annotations.NodeAccessType

  def checkRights(userCache: UserCache, userId: Option[Long]): annotations.NodeAccessType = {
    checkRights(userCache, userId, NodeAccessType.READ)
  }
}