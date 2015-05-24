/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security

import java.time.OffsetDateTime
import java.util.OptionalLong

import com.google.inject.Inject
import com.lessmarkup.Constants
import com.lessmarkup.dataobjects.{User, UserGroupMembership}
import com.lessmarkup.interfaces.annotations.CacheHandlerWithFactory
import com.lessmarkup.interfaces.cache._
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.module.Implements
import com.lessmarkup.interfaces.structure.{CachedNodeInformation, NodeAccessType, NodeCache}
import com.lessmarkup.interfaces.system.UserCache

import scala.collection.JavaConversions._

class UserCacheFactory @Inject() (domainModelProvider: DomainModelProvider, dataCache: DataCache) extends CacheHandlerFactory {
  override def createHandler(id: Long): CacheHandler = {
    new UserCacheImpl(domainModelProvider, dataCache, id)
  }
}

@Implements(classOf[UserCache])
@CacheHandlerWithFactory(classOf[UserCacheFactory])
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

  def getName: String = if (user.isDefined) user.get.getName else ""

  def isRemoved: Boolean = user.isEmpty || user.get.isRemoved

  def isAdministrator: Boolean = user.isDefined && user.get.isAdministrator

  def isApproved: Boolean = user.isDefined && user.get.isApproved

  def isEmailConfirmed: Boolean = user.isDefined && user.get.isEmailConfirmed

  def getGroups: List[Long] = {

    if (user.isEmpty) {
      return List()
    }

    val domainModel: DomainModel = domainModelProvider.create
    try {
      domainModel.query
        .from(classOf[UserGroupMembership])
        .where("userId = $", userId).toList(classOf[UserGroupMembership], Option("UserGroupId")).map(_.getUserGroupId)
    } finally {
      domainModel.close()
    }
  }

  def getEmail: String = if (user.isDefined) user.get.getEmail else ""

  def getTitle: String = if (user.isDefined) user.get.getTitle else ""

  def isBlocked: Boolean = {

    if (user.isEmpty) {
      return true
    }

    if (!user.get.isBlocked) {
      return false
    }

    if (user.get.getUnblockTime != null && user.get.getUnblockTime.isBefore(OffsetDateTime.now)) {
      return false
    }

    true
  }

  def getProperties: String = if (user.isDefined) user.get.getProperties else ""

  def getAvatarImageId: Option[Long] = {
    if (user.isDefined) {
      val imageId = user.get.getAvatarImageId
      if (imageId.isPresent) Option(imageId.getAsLong) else None
    } else {
      None
    }
  }

  def getUserImageId: Option[Long] = {
    if (user.isDefined) {
      val imageId = user.get.getUserImageId
      if (imageId.isPresent) Option(imageId.getAsLong) else None
    } else {
      None
    }
  }

  def getNodes: List[(CachedNodeInformation, NodeAccessType)] = {

    dataCache.get(classOf[NodeCache])
      .getNodes
      .toList
      .map(n => {
        val rights = n.checkRights(this, OptionalLong.of(userId))
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
      Option(user.get.getUnblockTime)
    }
  }
}