/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.structure

import com.lessmarkup.dataobjects.NodeAccess
import com.lessmarkup.interfaces.annotations.NodeAccessType

class CachedNodeAccess(nodeAccessType: NodeAccessType, userId: Option[Long], groupId: Option[Long], nodeId: Long) {

  def this(nodeAccess: NodeAccess) {
    this(nodeAccess.accessType, nodeAccess.userId, nodeAccess.groupId, nodeAccess.nodeId)
  }

  def getAccessType: NodeAccessType = nodeAccessType

  def getUserId: Option[Long] = userId

  def getGroupId: Option[Long] = groupId

  def getNodeId = nodeId
}
