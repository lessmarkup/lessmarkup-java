/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.cache

abstract class AbstractCacheHandler(val handledCollectionTypes: Seq[Class[_]] = Nil) extends CacheHandler {

  def initialize(objectId: Option[Long]) {
    if (objectId.isDefined) {
      throw new IllegalArgumentException
    }
  }

  def expires(collectionId: Int, entityId: Long, changeType: EntityChangeType) = true

  def isExpired: Boolean = false
}