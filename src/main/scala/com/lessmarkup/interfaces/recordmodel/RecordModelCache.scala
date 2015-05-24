package com.lessmarkup.interfaces.recordmodel

import com.lessmarkup.interfaces.cache.CacheHandler

trait RecordModelCache extends CacheHandler {
  def getDefinition(recordModelType: Class[_]): Option[RecordModelDefinition]
  def getDefinition(id: String): Option[RecordModelDefinition]
  def hasDefinition(recordModelType: Class[_]): Boolean
}
