package com.lessmarkup.interfaces.recordmodel

import com.google.gson.JsonElement
import com.lessmarkup.interfaces.data.DataObject
import scala.collection.JavaConverters._

trait RecordModelDefinition {
  def getTitleTextId: String

  def getModuleType: String

  def getModelType: Class[_ <: RecordModel[_]]

  def getDataType: Class[_ <: DataObject]

  def getId: String

  def getFields: List[InputFieldDefinition]

  @Deprecated
  def getFieldsJava = getFields.asJava

  def getColumns: List[RecordColumnDefinition]

  @Deprecated
  def getColumnsJava = getColumns.asJava

  def validateInput(objectToValidate: JsonElement, isNew: Boolean)

  def isSubmitWithCaptcha: Boolean

  def createModelCollection: ModelCollection[_]
}