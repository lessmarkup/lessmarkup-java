package com.lessmarkup.interfaces.data

import java.util.function.Predicate

import com.lessmarkup.interfaces.cache.CacheHandler

trait ChangesCache extends CacheHandler {
  def getLastChangeId: Option[Long]
  def getCollectionChanges(collectionId: Int, fromId: Option[Long], toId: Option[Long], filterFunc: Option[Predicate[DataChange]]): java.util.Collection[DataChange]
}
