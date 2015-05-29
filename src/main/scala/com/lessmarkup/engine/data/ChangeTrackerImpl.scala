/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data

import java.time.OffsetDateTime
import java.util.logging.{Level, Logger}

import com.google.inject.Inject
import com.lessmarkup.Constants
import com.lessmarkup.dataobjects.EntityChangeHistory
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.Implements
import com.lessmarkup.interfaces.cache.EntityChangeType
import com.lessmarkup.interfaces.data.{ChangeListener, ChangeTracker, DataObject, DomainModel, DomainModelProvider}

import scala.collection.JavaConversions._
import scala.collection.mutable

@Implements(classOf[ChangeTracker])
class ChangeTrackerImpl @Inject() (domainModelProvider: DomainModelProvider) extends ChangeTracker {

  private var changeTrackingInitialized: Boolean = false
  private final val syncObject: AnyRef = new AnyRef
  private var lastUpdateId: Long = 0L
  private val changeQueue: mutable.ListBuffer[EntityChangeHistory] = mutable.ListBuffer()
  private var listeners: List[ChangeListener] = List()

  private def handleQueue() {

    if (listeners.isEmpty) {
      syncObject synchronized {
        if (listeners.isEmpty) {
          changeQueue.clear()
          return
        }
      }
    }

    while (changeQueue.nonEmpty) {
      var change: Option[EntityChangeHistory] = None
      syncObject synchronized {
        change = changeQueue.headOption
        if (change.isDefined)
          changeQueue.remove(0)
      }
      if (change.isDefined) {
        listeners.foreach(listener => listener.onChange(
          change.get.id,
          change.get.userId,
          change.get.entityId,
          change.get.collectionId,
          EntityChangeType(change.get.changeType)))
      }
    }
  }

  def stop() {
  }

  private def initializeChangeTracker() {
    if (changeTrackingInitialized) {
      return
    }
    syncObject synchronized {
      if (changeTrackingInitialized) {
        return
      }
      changeTrackingInitialized = true
      val domainModel: DomainModel = domainModelProvider.create
      try {
        val history: Option[EntityChangeHistory] = domainModel.query
          .from(classOf[EntityChangeHistory])
          .orderByDescending(Constants.DataIdPropertyName)
          .first(classOf[EntityChangeHistory], None)
        if (history.isDefined) {
          lastUpdateId = history.get.id
        }
      }
      catch {
        case ex: Exception =>
          Logger.getLogger(classOf[ChangeTrackerImpl].getName).log(Level.SEVERE, null, ex)
      } finally {
        if (domainModel != null) domainModel.close()
      }
    }
  }

  def invalidate() {
    enqueueUpdates()
    handleQueue()
  }

  def addChange[T <: DataObject](`type`: Class[T], objectId: Long, changeType: EntityChangeType, domainModel: DomainModel) {
    val record: EntityChangeHistory = new EntityChangeHistory(
      entityId = objectId,
      changeType = changeType.value,
      userId = RequestContextHolder.getContext.getCurrentUser.getUserId,
      collectionId = MetadataStorage.getCollectionId(`type`).get,
      created = OffsetDateTime.now
    )
    domainModel.create(record)
  }

  def addChange[T <: DataObject](`type`: Class[T], dataObject: T, changeType: EntityChangeType, domainModel: DomainModel) {
    initializeChangeTracker()
    val record = new EntityChangeHistory(
      entityId = dataObject.id,
      changeType = changeType.value,
      userId = RequestContextHolder.getContext.getCurrentUser.getUserId,
      collectionId = MetadataStorage.getCollectionId(`type`).get,
      created = OffsetDateTime.now
    )
    domainModel.create(record)
  }

  def subscribe(listener: ChangeListener) {
    initializeChangeTracker()
    listeners = listener :: listeners
  }

  def unsubscribe(listener: ChangeListener) {
    initializeChangeTracker()
    listeners = listeners.filter(_ != listener)
  }

  def enqueueUpdates() {
    val domainModel: DomainModel = domainModelProvider.create
    try {
      domainModel.query
        .from(classOf[EntityChangeHistory])
        .where("id > " + lastUpdateId).orderBy("id")
        .toList(classOf[EntityChangeHistory])
        .foreach(h => {
        lastUpdateId = h.id
        syncObject synchronized {
          changeQueue.add(h)
        }
      })
    }
    catch {
      case ex: Exception =>
        Logger.getLogger(classOf[ChangeTrackerImpl].getName).log(Level.SEVERE, null, ex)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }
}
