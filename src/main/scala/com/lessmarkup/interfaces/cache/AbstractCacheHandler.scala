package com.lessmarkup.interfaces.cache

abstract class AbstractCacheHandler(handledCollectionTypes: Array[Class[_]]) extends CacheHandler {

  def initialize(objectId: Option[Long]) {
    if (objectId.isDefined) {
      throw new IllegalArgumentException
    }
  }

  def expires(collectionId: Int, entityId: Long, changeType: EntityChangeType): Boolean = {
    true
  }

  def getHandledCollectionTypes: Option[List[Class[_]]] = Option(handledCollectionTypes.toList)

  def isExpired: Boolean = false
}