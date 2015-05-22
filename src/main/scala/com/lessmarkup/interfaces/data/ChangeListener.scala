package com.lessmarkup.interfaces.data

import java.util.OptionalLong
import com.lessmarkup.interfaces.cache.EntityChangeType

trait ChangeListener {
  def onChange(recordId: Long, userId: OptionalLong, entityId: Long, collectionId: Int, changeType: EntityChangeType)
}