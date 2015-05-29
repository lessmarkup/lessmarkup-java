/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

import com.lessmarkup.interfaces.cache.EntityChangeType

trait ChangeTracker {
  def invalidate()

  def addChange[T <: DataObject](`type`: Class[T], objectId: Long, changeType: EntityChangeType, domainModel: DomainModel)

  def addChange[T <: DataObject](`type`: Class[T], dataObject: T, changeType: EntityChangeType, domainModel: DomainModel)

  def subscribe(listener: ChangeListener)

  def unsubscribe(listener: ChangeListener)

  def enqueueUpdates()
}
