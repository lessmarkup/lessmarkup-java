package com.lessmarkup.interfaces.system

import java.time.OffsetDateTime
import com.lessmarkup.interfaces.cache.CacheHandler
import com.lessmarkup.interfaces.structure.{CachedNodeInformation, NodeAccessType}
import scala.collection.JavaConverters._

trait UserCache extends CacheHandler {
  def getName: String

  def isRemoved: Boolean

  def isAdministrator: Boolean

  def isApproved: Boolean

  def isEmailConfirmed: Boolean

  def getGroups: List[Long]

  @Deprecated
  def getGroupsJava = getGroups.map(g => g.asInstanceOf[java.lang.Long]).asJava

  def getEmail: String

  def getTitle: String

  def isBlocked: Boolean

  def getUnblockTime: Option[OffsetDateTime]

  def getProperties: String

  def getAvatarImageId: Option[Long]

  def getUserImageId: Option[Long]

  def getNodes: List[(CachedNodeInformation, NodeAccessType)]

  @Deprecated
  def getNodesJava = getNodes.asJava
}