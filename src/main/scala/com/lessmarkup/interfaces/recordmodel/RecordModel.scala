package com.lessmarkup.interfaces.recordmodel

import com.lessmarkup.framework.helpers.DependencyResolver
import com.lessmarkup.interfaces.data.DataObject

abstract class RecordModel[T](titleTextId: String, collectionType: Class[_ <: ModelCollection[T]], dataType: Class[_ <: DataObject], submitWithCaptcha: Boolean) {
  private var id: Long = 0L

  protected def this(titleTextId: String, collectionType: Class[_ <: ModelCollection[T]], dataType: Class[_ <: DataObject]) {
    this(titleTextId, collectionType, dataType, false)
  }

  protected def this(titleTextId: String, collectionType: Class[_ <: ModelCollection[T]]) {
    this(titleTextId, collectionType, null)
  }

  protected def this(titleTextId: String) {
    this(titleTextId, null)
  }

  protected def this(titleTextId: String, submitWithCaptcha: Boolean) {
    this(titleTextId, null, null, submitWithCaptcha)
  }

  protected def this(collectionType: Class[_ <: ModelCollection[T]], dataType: Class[_ <: DataObject]) {
    this(null, collectionType, dataType)
  }

  protected def this() {
    this(null)
  }

  def getDataType: Class[_ <: DataObject] = dataType

  def getTitleTextId: String = titleTextId

  def getSubmitWithCaptcha: Boolean = submitWithCaptcha

  def createCollection: ModelCollection[T] = DependencyResolver.resolve(this.collectionType)

  def getId: Long = id

  def setId(id: Long) {
    this.id = id
  }
}
