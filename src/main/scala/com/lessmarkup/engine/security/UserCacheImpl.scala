/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security

import java.time.OffsetDateTime
import com.google.inject.Inject
import com.lessmarkup.Constants
import com.lessmarkup.dataobjects.{User, UserGroupMembership}
import com.lessmarkup.interfaces.annotations.{Implements, NodeAccessType, UseInstanceFactory}
import com.lessmarkup.interfaces.cache._
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.structure.{CachedNodeInformation, NodeCache}
import com.lessmarkup.interfaces.system.UserCache

class UserCacheFactory @Inject() (domainModelProvider: DomainModelProvider, dataCache: DataCache) extends InstanceFactory {
  override def createInstance(params: Any*): CacheHandler = {
    new UserCacheImpl(domainModelProvider, dataCache, params.head.asInstanceOf)
  }
}

@Implements(classOf[UserCache])
@UseInstanceFactory(classOf[UserCacheFactory])
class UserCacheImpl (domainModelProvider: DomainModelProvider, dataCache: DataCache, userId: Long) extends AbstractCacheHandler(Array[Class[_]](classOf[User])) with UserCache {

  private val user = loadUser

  private def loadUser: Option[User] = {
    val domainModel: DomainModel = domainModelProvider.create
    try {
      domainModel.query
        .from(classOf[User])
        .where(Constants.DataIdPropertyName + " = $", userId)
        .first(classOf[User], None)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def getName: String = if (user.isDefined) user.get.name else ""

  def isRemoved: Boolean = user.isEmpty || user.get.removed

  def isAdministrator: Boolean = user.isDefined && user.get.administrator

  def isApproved: Boolean = user.isDefined && user.get.approved

  def isEmailConfirmed: Boolean = user.isDefined && user.get.emailConfirmed

  def getGroups: Seq[Long] = {

    if (user.isEmpty) {
      return List()
    }

    val domainModel: DomainModel = domainModelProvider.create
    try {
      domainModel.query
        .from(classOf[UserGroupMembership])
        .where("userId = $", userId).toList(classOf[UserGroupMembership], Option("UserGroupId")).map(_.userGroupId)
    } finally {
      domainModel.close()
    }
  }

  def getEmail: String = if (user.isDefined) user.get.email else ""

  def getTitle: String = if (user.isDefined) user.get.title.getOrElse("") else ""

  def isBlocked: Boolean = {

    if (user.isEmpty) {
      return true
    }

    if (!user.get.blocked) {
      return false
    }

    if (user.get.unblockTime.isDefined && user.get.unblockTime.get.isBefore(OffsetDateTime.now)) {
      return false
    }

    true
  }

  def getProperties: String = if (user.isDefined) user.get.properties.getOrElse("") else ""

  def getAvatarImageId: Option[Long] = {
    if (user.isDefined) {
      val imageId = user.get.avatarImageId
      if (imageId.isDefined) Option(imageId.get) else None
    } else {
      None
    }
  }

  def getUserImageId: Option[Long] = {
    if (user.isDefined) {
      val imageId = user.get.userImageId
      if (imageId.isDefined) Option(imageId.get) else None
    } else {
      None
    }
  }

  def getNodes: List[(CachedNodeInformation, NodeAccessType)] = {

    dataCache.get(classOf[NodeCache])
      .getNodes
      .toList
      .map(n => {
        val rights = n.checkRights(this, Option(userId))
        if (rights == NodeAccessType.NO_ACCESS) {
          None
        } else {
          Option((n, rights))
        }
      })
      .filter(_.isDefined)
      .map(_.get)
  }

  def getUnblockTime: Option[OffsetDateTime] = {
    if (user.isEmpty) {
      None
    } else {
      user.get.unblockTime
    }
  }
}