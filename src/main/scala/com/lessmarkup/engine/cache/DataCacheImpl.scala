/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.cache

import java.util.Random

import com.google.inject.{Inject, Singleton}
import com.lessmarkup.framework.helpers.DependencyResolver
import com.lessmarkup.interfaces.annotations.Implements
import com.lessmarkup.interfaces.cache._
import com.lessmarkup.interfaces.data.{ChangeListener, ChangeTracker, DomainModelProvider}

import scala.collection.JavaConversions._

@Implements(classOf[DataCache])
@Singleton
class DataCacheImpl @Inject()(changeTracker: ChangeTracker) extends DataCache with ChangeListener {

  private class CacheItem(final val itemType: Class[_], final val objectId: Option[Long], final val cacheHandler: CacheHandler)

  private var hashedCollectionIds: Set[(Int, CacheItem)] = Set()
  private var items: Map[(Class[_], Option[Long]), CacheItem] = Map()

  changeTracker.subscribe(this)

  private def getHandlerCollectionIds(cacheHandler: CacheHandler, domainModelProvider: DomainModelProvider): Seq[Int] = {
    val collectionTypes = cacheHandler.handledCollectionTypes
    if (collectionTypes.isEmpty) {
      return Nil
    }

    for (collectionType <- collectionTypes;
         collectionId = domainModelProvider.getCollectionId(collectionType)
         if collectionId.isDefined
    ) yield {
      collectionId.get
    }
  }

  private def getHashedCollectionIds(cacheItem: CacheItem, domainModelProvider: DomainModelProvider): Seq[(Int, CacheItem)] =
    getHandlerCollectionIds(cacheItem.cacheHandler, domainModelProvider).map(id => (id, cacheItem))

  private def set[T <: CacheHandler](itemType: Class[T], cachedObject: T, objectId: Option[Long]) {

    val key: (Class[_], Option[Long]) = (itemType, objectId)
    val cacheItem: CacheItem = new CacheItem(itemType, objectId, cachedObject)
    val exists = this.items.contains(key)

    items += ((key, cacheItem))

    if (exists) {
      return
    }

    val collectionTypes = Option(cachedObject.handledCollectionTypes)

    if (collectionTypes.isEmpty) {
      return
    }

    val domainModelProvider: DomainModelProvider = DependencyResolver(classOf[DomainModelProvider])

    hashedCollectionIds ++= getHashedCollectionIds(cacheItem, domainModelProvider)
  }

  def createInstance[T <: CacheHandler](itemType: Class[T], objectId: Option[Long]): Unit = {

    val newInstance = if (objectId.isDefined) {
      DependencyResolver(itemType, objectId.get)
    } else {
      DependencyResolver(itemType)
    }

    set(itemType, newInstance, objectId)
  }

  def get[T <: CacheHandler](itemType: Class[T], objectId: Option[Long], create: Boolean): Option[T] = {

    val key: (Class[_], Option[Long]) = (itemType, objectId)

    var ret = this.items.get(key)

    if (ret.isDefined) {

      val cacheItem: CacheItem = ret.get

      if (cacheItem.cacheHandler.isExpired) {
        this.items.remove(key)
      }
      else {
        val obj = ret.get.cacheHandler
        if (itemType.isInstance(obj)) {
          return Option(itemType.cast(obj))
        }
        else {
          return None
        }
      }
    }

    if (!create) {
      return None
    }

    createInstance(itemType, objectId)

    ret = items.get(key)
    val obj = ret.get.cacheHandler

    if (itemType.isInstance(obj))
      Option(itemType.cast(obj))
    else
      None
  }

  def expired[T <: CacheHandler](itemType: Class[T], objectId: Option[Long]) {
    remove(itemType, objectId)
  }

  def createWithUniqueId[T <: CacheHandler](itemType: Class[T]): T = {
    val random: Random = new Random

    val uniqueKey = Stream.continually(random.nextLong())
      .dropWhile(r => items.containsKey((itemType, r)))
      .head

    get(itemType, Option(uniqueKey), create = true).get
  }

  def reset() {
    hashedCollectionIds = Set()
    items = Map()
  }

  private def remove(key: (Class[_], Option[Long])) {

    val cacheItem: Option[CacheItem] = items.get(key)
    if (cacheItem.isEmpty) {
      return
    }

    val domainModelProvider: DomainModelProvider = DependencyResolver(classOf[DomainModelProvider])

    hashedCollectionIds --= getHashedCollectionIds(cacheItem.get, domainModelProvider)

    items -= key
  }

  def onChange(recordId: Long, userId: Option[Long], entityId: Long, collectionId: Int, changeType: EntityChangeType) {
    for (
      (id, item) <- hashedCollectionIds
      if id == collectionId
      if item.cacheHandler.expires(collectionId, entityId, changeType)
    ) {
      remove(item.itemType, item.objectId)
    }
  }
}