package com.lessmarkup.engine.cache

import java.util.{Optional, OptionalLong, Random}

import com.google.inject.{Inject, Singleton}
import com.lessmarkup.framework.helpers.{DependencyResolver, LoggingHelper}
import com.lessmarkup.interfaces.cache.{CacheHandler, DataCache, EntityChangeType}
import com.lessmarkup.interfaces.data.{ChangeListener, ChangeTracker, DomainModelProvider}
import com.lessmarkup.interfaces.module.Implements
import scala.collection.JavaConversions._

@Implements(classOf[DataCache])
@Singleton
class DataCacheImpl @Inject()(changeTracker: ChangeTracker) extends DataCache with ChangeListener {

  private class CacheItem(final val itemType: Class[_], final val objectId: Option[Long], final val cacheHandler: CacheHandler)

  private var hashedCollectionIds: Set[(Int, CacheItem)] = Set()
  private var items: Map[(Class[_], Option[Long]), CacheItem] = Map()

  changeTracker.subscribe(this)

  private def getHandlerCollectionIds(cacheHandler: CacheHandler, domainModelProvider: DomainModelProvider): List[Int] = {
    val collectionTypes = Option(cacheHandler.getHandledCollectionTypes)
    if (collectionTypes.isEmpty) {
      return List[Int]()
    }
    collectionTypes.get.toList.map(collectionType => {
      val collectionId = domainModelProvider.getCollectionId(collectionType)
      if (collectionId.isPresent) {
        Option(collectionId.getAsInt)
      } else {
        None
      }
    }).filter(_.isDefined).map(_.get)
  }

  private def getHashedCollectionIds(cacheItem: CacheItem, domainModelProvider: DomainModelProvider): List[(Int, CacheItem)] =
    getHandlerCollectionIds(cacheItem.cacheHandler, domainModelProvider).map(id => (id, cacheItem))

  private def set[T <: CacheHandler](itemType: Class[T], cachedObject: T, objectId: Option[Long]) {

    val key: (Class[_], Option[Long]) = (itemType, objectId)
    val cacheItem: CacheItem = new CacheItem(itemType, objectId, cachedObject)
    val exists = this.items.contains(key)

    items += ((key, cacheItem))

    if (exists) {
      return
    }

    val collectionTypes = Option(cachedObject.getHandledCollectionTypes)

    if (collectionTypes.isEmpty) {
      return
    }

    val domainModelProvider: DomainModelProvider = DependencyResolver.resolve(classOf[DomainModelProvider])

    hashedCollectionIds ++= getHashedCollectionIds(cacheItem, domainModelProvider)
  }

  private def get[T <: CacheHandler](itemType: Class[T], objectId: Option[Long], create: Boolean): Option[T] = {

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

    val newObject: T = DependencyResolver.resolve(itemType)

    set(itemType, newObject, objectId)

    try {
      newObject.initialize(if (objectId.isDefined) OptionalLong.of(objectId.get) else OptionalLong.empty())
    }
    catch {
      case e: Exception =>
        LoggingHelper.logException(getClass, e)
        remove(key)
        throw e
    }
    ret = items.get(key)
    val obj = ret.get.cacheHandler

    if (itemType.isInstance(obj))
      Option(itemType.cast(obj))
    else
      None
  }

  def get[T <: CacheHandler](itemType: Class[T], objectId: OptionalLong, create: Boolean): Optional[T] = {
    val localObjectId = if (objectId.isPresent) Option(objectId.getAsLong) else None
    val ret = get(itemType, localObjectId, create)
    if (ret.isDefined)
      Optional.of(ret.get)
    else
      Optional.empty()
  }

  def get[T <: CacheHandler](itemType: Class[T], objectId: OptionalLong): T = {
    get(itemType, objectId, create = true).get()
  }

  def get[T <: CacheHandler](itemType: Class[T]): T = {
    get(itemType, OptionalLong.empty)
  }

  def expired[T <: CacheHandler](itemType: Class[T], objectId: OptionalLong) {
    remove(itemType, if (objectId.isPresent) Option(objectId.getAsLong) else None)
  }

  def createWithUniqueId[T <: CacheHandler](itemType: Class[T]): T = {
    val random: Random = new Random

    val uniqueKey = Stream.continually(random.nextLong())
      .map(r => (itemType, r))
      .dropWhile(r => items.containsKey(r))
      .head
      ._2

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

    val domainModelProvider: DomainModelProvider = DependencyResolver.resolve(classOf[DomainModelProvider])

    hashedCollectionIds --= getHashedCollectionIds(cacheItem.get, domainModelProvider)

    items -= key
  }

  def onChange(recordId: Long, userId: OptionalLong, entityId: Long, collectionId: Int, changeType: EntityChangeType) {

    hashedCollectionIds
      .filter(_._1 == collectionId)
      .map(_._2)
      .filter(_.cacheHandler.expires(collectionId, entityId, changeType))
      .foreach(h => {
      remove(h.itemType, h.objectId)
    })
  }
}