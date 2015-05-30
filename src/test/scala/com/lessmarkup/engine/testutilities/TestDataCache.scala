/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.testutilities

import com.lessmarkup.interfaces.cache.{CacheHandler, DataCache}
import scala.collection.mutable

class TestDataCache extends DataCache {

  val objectMap: mutable.Map[(Class[_ <: CacheHandler], Option[Long]), CacheHandler] = mutable.LinkedHashMap()

  def set[T <: CacheHandler](t: Class[T], objectId: Option[Long], handler: T): Unit = {
    val key = (t, objectId)
    objectMap += key -> handler
  }

  override def get[T <: CacheHandler](t: Class[T], objectId: Option[Long], create: Boolean): Option[T] = {
    val handler: T = objectMap((t, objectId)).asInstanceOf[T]
    Option(handler)
  }

  override def expired[T <: CacheHandler](t: Class[T], objectId: Option[Long]): Unit = {}

  override def createWithUniqueId[T <: CacheHandler](t: Class[T]): T = ???

  override def reset(): Unit = {}
}
