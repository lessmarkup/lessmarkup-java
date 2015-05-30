/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.cache

trait DataCache {

  def get[T <: CacheHandler] (t: Class[T], objectId: Option[Long], create: Boolean): Option[T]
  def get[T <: CacheHandler] (t: Class[T], objectId: Option[Long]): T = get(t, objectId, create = true).get
  def get[T <: CacheHandler] (t: Class[T]): T = get(t, None)
  def expired[T <: CacheHandler] (t: Class[T], objectId: Option[Long])
  def createWithUniqueId[T <: CacheHandler] (t: Class[T]): T
  def reset()
}
