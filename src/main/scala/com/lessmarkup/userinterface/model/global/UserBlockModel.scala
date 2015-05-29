/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.global

import java.time.OffsetDateTime

import com.google.inject.Inject
import com.lessmarkup.dataobjects.{User, UserBlockHistory}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.EntityChangeType
import com.lessmarkup.interfaces.data.{ChangeTracker, DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.recordmodel.RecordModel

class UserBlockModel @Inject() (domainModelProvider: DomainModelProvider, changeTracker: ChangeTracker) extends RecordModel[UserBlockModel] {

  var reason: String = null
  var internalReason: String = null
  var unblockTime: OffsetDateTime = null

  def blockUser(userId: Long) {
    val currentUserId = RequestContextHolder.getContext.getCurrentUser.getUserId
    if (currentUserId.isEmpty) {
      throw new IllegalArgumentException
    }
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val optionUser = domainModel.query.from(classOf[User]).find(classOf[User], userId)
      if (optionUser.isEmpty) {
        throw new IllegalArgumentException
      }
      val user = optionUser.get
      user.blocked = true
      user.blockReason = reason
      if (unblockTime != null && unblockTime.isBefore(OffsetDateTime.now)) {
        unblockTime = null
      }
      user.unblockTime = unblockTime
      user.lastBlock = OffsetDateTime.now
      val blockHistory: UserBlockHistory = new UserBlockHistory(
        blockedByUserId = currentUserId.get,
        blockedToTime = unblockTime,
        reason = reason,
        internalReason = internalReason,
        userId = userId,
        created = OffsetDateTime.now,
        unblocked = false
      )
      domainModel.create(blockHistory)
      changeTracker.addChange(classOf[User], user, EntityChangeType.UPDATED, domainModel)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def unblockUser(userId: Long) {
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val user = domainModel.query.from(classOf[User]).find(classOf[User], userId)
      if (user.isEmpty || !user.get.blocked) {
        return
      }
      user.get.blocked = false
      for (history <- domainModel.query.from(classOf[UserBlockHistory]).where("userId = $ AND unblocked = $", userId, false).toList(classOf[UserBlockHistory])) {
        history.unblocked = true
        domainModel.update(history)
      }
      changeTracker.addChange(classOf[User], user.get, EntityChangeType.UPDATED, domainModel)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }
}