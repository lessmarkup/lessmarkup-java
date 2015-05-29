/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.cache

trait CacheHandler {
  def initialize(objectId: Option[Long])
  def expires(collectionId: Int, entityId: Long, changeType: EntityChangeType): Boolean
  def handledCollectionTypes: Seq[Class[_]]
  def isExpired: Boolean
}
