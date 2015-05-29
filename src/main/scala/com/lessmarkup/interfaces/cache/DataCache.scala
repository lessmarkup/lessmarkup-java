/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.cache

import java.util.OptionalLong

trait DataCache {

  def get[T <: CacheHandler] (t: Class[T], objectId: Option[Long], create: Boolean): Option[T]

  @Deprecated
  def getJava[T <: CacheHandler] (t: Class[T], objectId: OptionalLong, create: Boolean): T = {
    val ret = get(t, if (objectId.isPresent) Option(objectId.getAsLong) else None, create)
    if (ret.isDefined) ret.get else null.asInstanceOf
  }

  def get[T <: CacheHandler] (t: Class[T], objectId: Option[Long]): T = get(t, objectId, create = true).get

  @Deprecated
  def getJava[T <: CacheHandler] (t: Class[T], objectId: OptionalLong): T = {
    get(t, if (objectId.isPresent) Option(objectId.getAsLong) else None)
  }

  def get[T <: CacheHandler] (t: Class[T]): T = get(t, None)

  def expired[T <: CacheHandler] (t: Class[T], objectId: Option[Long])

  @Deprecated
  def expiredJava[T <: CacheHandler] (t: Class[T], objectId: OptionalLong) = {
    expired(t, if (objectId.isPresent) Option(objectId.getAsLong) else None)
  }

  def createWithUniqueId[T <: CacheHandler] (t: Class[T]): T
  def reset()
}
