package com.lessmarkup.interfaces.data

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.cache.EntityChangeType

trait DataChange {
  def getId: Long
  def getEntityId: Long
  def getCreated: OffsetDateTime
  def getUserId: Option[Long]
  def getParameter1: Long
  def getParameter2: Long
  def getParameter3: Long
  def getType: EntityChangeType
}