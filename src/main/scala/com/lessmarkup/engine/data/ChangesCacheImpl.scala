/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data

import java.time.OffsetDateTime
import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}
import java.util.logging.{Level, Logger}

import com.google.inject.Inject
import com.lessmarkup.Constants
import com.lessmarkup.dataobjects.EntityChangeHistory
import com.lessmarkup.interfaces.annotations.Implements
import com.lessmarkup.interfaces.cache.{AbstractCacheHandler, EntityChangeType}
import com.lessmarkup.interfaces.data.{ChangesCache, DataChange, DomainModel, DomainModelProvider, QueryBuilder}

import scala.collection.JavaConversions._
import scala.collection.mutable

class Change(id: Long,
             entityId: Long,
             created: OffsetDateTime,
             userId: Option[Long],
             parameter1: Long,
             parameter2: Long,
             parameter3: Long,
             changeType: EntityChangeType) extends DataChange {

  def getId = id
  def getEntityId = entityId
  def getCreated = created
  def getUserId: Option[Long] = userId
  def getParameter1 = parameter1
  def getParameter2: Long = parameter2
  def getParameter3 = parameter3
  def getType = changeType
}

object ChangesCacheImpl {
  private val UPDATE_INTERVAL: Int = 500
}

@Implements(classOf[ChangesCache])
class ChangesCacheImpl @Inject() (domainModelProvider: DomainModelProvider) extends AbstractCacheHandler(null) with ChangesCache {
  private var lastUpdateId: Option[Long] = None
  private var lastUpdateTime: Long = 0L
  private final val lock: ReadWriteLock = new ReentrantReadWriteLock
  private final val changes: mutable.HashMap[Integer, mutable.ListBuffer[Change]] = mutable.HashMap()

  private def updateIfRequired() {
    if (System.currentTimeMillis - lastUpdateTime < ChangesCacheImpl.UPDATE_INTERVAL) {
      return
    }
    lock.writeLock.lock()
    try {
      lastUpdateTime = System.currentTimeMillis
      val domainModel: DomainModel = domainModelProvider.create
      try {
        val dateFrame: OffsetDateTime = OffsetDateTime.now.minusDays(1)
        var query: QueryBuilder = domainModel.query.from(classOf[EntityChangeHistory])
        if (lastUpdateId.isEmpty) {
          query = query.where("created >= $", dateFrame)
        }
        else {
          query = query.where("created >= $ AND " + Constants.DataIdPropertyName + " > $", dateFrame, new java.lang.Long(lastUpdateId.get))
        }
        query.toList(classOf[EntityChangeHistory]).foreach(history => {
          lastUpdateId = Option(history.id)
          var collection: Option[mutable.ListBuffer[Change]] = changes.get(history.collectionId)
          if (collection.isEmpty) {
            collection = Option(new mutable.ListBuffer[Change]())
            changes.put(history.collectionId, collection.get)
          }

          val change = new Change(
            id = history.id,
            entityId = history.entityId,
            created = history.created,
            userId = if (history.userId.isDefined) Option(history.userId.get) else None,
            parameter1 = history.parameter1,
            parameter2 = history.parameter2,
            parameter3 = history.parameter3,
            changeType = EntityChangeType(history.changeType)
          )

          collection.get.add(change)

          if (collection.get.head.getCreated.isBefore(dateFrame)) {
            collection.get.filter(h => h.getCreated.isBefore(dateFrame)).foreach(h => {
              collection.get.remove(h)
            })
          }
        })
      }
      catch {
        case ex: Exception =>
          Logger.getLogger(classOf[ChangesCacheImpl].getName).log(Level.SEVERE, null, ex)
      } finally {
        if (domainModel != null) domainModel.close()
      }
    } finally {
      lock.writeLock.unlock()
    }
  }

  def getLastChangeId: Option[Long] = {
    updateIfRequired()
    lastUpdateId
  }

  def getCollectionChanges(collectionId: Int, fromId: Option[Long], toId: Option[Long], filterFunc: Option[DataChange => Boolean]): Seq[DataChange] = {
    updateIfRequired()
    lock.readLock.lock()
    try {
      val collection: Option[mutable.ListBuffer[Change]] = changes.get(collectionId)
      if (collection == null) {
        return new java.util.LinkedList[DataChange]()
      }
      var query: mutable.ListBuffer[Change] = collection.get
      if (fromId.isDefined) {
        query = query.filter(c => c.getId > fromId.get)
      }
      if (toId.isDefined) {
        query = query.filter(c => c.getId <= toId.get)
      }
      if (filterFunc != null) {
        query = query.filter(c => filterFunc.get(c))
      }
      query.toSeq
    } finally {
      lock.readLock.unlock()
    }
  }
}