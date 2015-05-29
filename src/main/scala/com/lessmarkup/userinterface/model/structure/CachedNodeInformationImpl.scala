package com.lessmarkup.userinterface.model.structure

import com.lessmarkup.dataobjects.Node
import com.lessmarkup.interfaces.annotations.NodeAccessType
import com.lessmarkup.interfaces.security.CurrentUser
import com.lessmarkup.interfaces.structure.{CachedNodeAccess, CachedNodeInformation, NodeHandlerFactory}
import com.lessmarkup.interfaces.system.UserCache

object CachedNodeInformationImpl {
  def appliesTo(nodeAccess: CachedNodeAccess, userId: Option[Long], groupIds: Seq[Long]): Boolean = {
    if (userId.isEmpty) {
      return nodeAccess.getUserId.isEmpty && nodeAccess.getGroupId.isEmpty
    }
    if (nodeAccess.getUserId.isDefined) {
      if (userId.isEmpty) {
        return false
      }
      return nodeAccess.getUserId.get == userId.get
    }
    if (nodeAccess.getGroupId.isDefined) {
      return groupIds != null && groupIds.contains(nodeAccess.getUserId.get)
    }
    false
  }
}

class CachedNodeInformationImpl(node: Node,
                                access: Option[Seq[CachedNodeAccess]],
                                val fullPath: String,
                                val children: Seq[CachedNodeInformation],
                                val loggedIn: Boolean = false,
                                handler: Option[(Class[_ <: NodeHandlerFactory], String)] = None) extends CachedNodeInformation {
  val nodeId: Long = node.id
  val enabled: Boolean = node.enabled
  val path: String = node.path
  val order: Int = 0
  val level: Int = 0
  val title: String = node.title
  val description: String = node.description
  val handlerId: String = node.handlerId
  var parent: Option[CachedNodeInformation] = None
  val accessList: Seq[CachedNodeAccess] = if (access.isDefined) access.get else Seq()
  val handlerModuleType: Option[String] = if (handler.isDefined) Option(handler.get._2) else None
  val settings: String = null
  var root: CachedNodeInformation = null
  val addToMenu: Boolean = node.addToMenu
  val parentNodeId: Option[Long] = node.parentId
  val handlerType: Option[Class[_ <: NodeHandlerFactory]] = if (handler.isDefined) Option(handler.get._1) else None

  private def checkRights(userId: Option[Long], groupIds: Option[Seq[Long]], accessType: NodeAccessType): NodeAccessType = {

    val fromParentAccessType = if (parent != null) {
      parent.asInstanceOf[CachedNodeInformationImpl].checkRights(userId, groupIds, accessType)
    } else {
      accessType
    }

    if (accessList.isEmpty) {
      return fromParentAccessType
    }

    val maxAccess: Option[CachedNodeAccess] = accessList.map(a => Option(a)).maxBy(a => a.get.getAccessType.getLevel)

    if (maxAccess.isDefined && (accessType.getLevel > maxAccess.get.getAccessType.getLevel)) {
      maxAccess.get.getAccessType
    } else {
      accessType
    }
  }

  def checkRights(currentUser: CurrentUser, defaultAccessType: NodeAccessType): NodeAccessType = {
    if (currentUser.isAdministrator) {
      return NodeAccessType.MANAGE
    }
    val accessType: NodeAccessType = checkRights(currentUser.getUserId, currentUser.getGroups, defaultAccessType)
    if (accessType != null && accessType != NodeAccessType.NO_ACCESS && (!currentUser.isApproved || !currentUser.emailConfirmed)) {
      NodeAccessType.READ
    } else {
      accessType
    }
  }

  def checkRights(userCache: UserCache, userId: Option[Long], defaultAccessType: NodeAccessType): NodeAccessType = {
    if (userCache.isAdministrator) {
      return NodeAccessType.MANAGE
    }
    val accessType: NodeAccessType = checkRights(userId, Option(userCache.getGroups), defaultAccessType)
    if (accessType != NodeAccessType.NO_ACCESS && (!userCache.isApproved || !userCache.isEmailConfirmed)) {
      NodeAccessType.READ
    } else {
      accessType
    }
  }
}
