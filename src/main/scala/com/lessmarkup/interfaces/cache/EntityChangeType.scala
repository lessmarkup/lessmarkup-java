package com.lessmarkup.interfaces.cache

object EntityChangeType {

  def apply(value: Int) = new EntityChangeType(value)

  final val ADDED = EntityChangeType(1)
  final val REMOVED = EntityChangeType(2)
  final val UPDATED = EntityChangeType(3)
}

class EntityChangeType(val value: Int)
