/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.testutilities

import com.lessmarkup.engine.cache.DataCacheImpl
import com.lessmarkup.interfaces.cache.CacheHandler
import com.lessmarkup.interfaces.data.ChangeTracker
import scala.collection.mutable

class TestDataCache(changeTracker: ChangeTracker) extends DataCacheImpl(changeTracker) {

  private val objectMap: mutable.Map[(Class[_ <: CacheHandler], Option[Long]), CacheHandler] = mutable.LinkedHashMap()

  def set[T <: CacheHandler](t: Class[T], objectId: Option[Long], handler: T): Unit = {
    val key = (t, objectId)
    objectMap += key -> handler
  }

  override def get[T <: CacheHandler](t: Class[T], objectId: Option[Long], create: Boolean): Option[T] = {

    val handler = objectMap.get((t, objectId))
    if (handler.isDefined) {
      Option(handler.get.asInstanceOf[T])
    } else {
      super.get[T](t, objectId, create)
    }
  }

  override def expired[T <: CacheHandler](t: Class[T], objectId: Option[Long]): Unit = {}

  override def createWithUniqueId[T <: CacheHandler](t: Class[T]): T = ???

  override def reset(): Unit = {}
}
