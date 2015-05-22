package com.lessmarkup.interfaces.cache

import java.util.{Optional, OptionalLong}

trait DataCache {
  def get[T <: CacheHandler] (t: Class[T], objectId: OptionalLong, create: Boolean): Optional[T]
  def get[T <: CacheHandler] (t: Class[T], objectId: OptionalLong): T
  def get[T <: CacheHandler] (t: Class[T]): T
  def expired[T <: CacheHandler] (t: Class[T], optionalLong: OptionalLong)
  def createWithUniqueId[T <: CacheHandler] (t: Class[T]): T
  def reset()
}
