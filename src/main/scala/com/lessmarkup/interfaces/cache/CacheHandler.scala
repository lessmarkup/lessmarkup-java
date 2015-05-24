package com.lessmarkup.interfaces.cache

trait CacheHandler {
  def initialize(objectId: Option[Long])
  def expires(collectionId: Int, entityId: Long, changeType: EntityChangeType): Boolean
  def getHandledCollectionTypes: Option[List[Class[_]]]
  def isExpired: Boolean
}
