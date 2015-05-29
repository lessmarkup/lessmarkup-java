package com.lessmarkup.userinterface.model.recordmodel

import com.google.gson.{JsonArray, JsonElement, JsonObject}
import com.lessmarkup.framework.helpers.{LanguageHelper, StringHelper}
import com.lessmarkup.interfaces.annotations.InputFieldType
import com.lessmarkup.interfaces.recordmodel.{InputFieldDefinition, RecordModelDefinition, SelectValueModel}

class InputFieldModel(source: InputFieldDefinition, definition: RecordModelDefinition, selectValues: Seq[SelectValueModel] = List()) {
  private final val text: String = LanguageHelper.getFullTextId(definition.getModuleType, source.getTextId)
  private final val fieldType: InputFieldType = source.getType
  private final val readOnly: Boolean = source.isReadOnly
  private final val id: String = source.getId
  private final val required: Boolean = source.isRequired
  private final val width: Double = source.getWidth
  private final val minWidth: Int = source.getMinWidth
  private final val maxWidth: Int = source.getMaxWidth
  private final val position: Int = source.getPosition
  private final val readOnlyCondition: String = source.getReadOnlyCondition
  private final val visibleCondition: String = source.getVisibleCondition
  private final val property: String = StringHelper.toJsonCase(source.getProperty.getName)
  private final val helpText: String = null
  private final val defaultValue: String = source.getDefaultValue
  private final val propertyType: Class[_] = source.getProperty.getType

  def getSelectValues: Seq[SelectValueModel] = {
    selectValues
  }

  def getType: InputFieldType = {
    this.fieldType
  }

  def toJson: JsonElement = {
    val ret: JsonObject = new JsonObject
    ret.addProperty("text", this.text)
    ret.addProperty("type", this.fieldType.toString)
    ret.addProperty("readOnly", this.readOnly)
    ret.addProperty("id", StringHelper.toJsonCase(this.id))
    ret.addProperty("required", this.required)
    ret.addProperty("width", this.width)
    ret.addProperty("minWidth", this.minWidth)
    ret.addProperty("maxWidth", this.maxWidth)
    ret.addProperty("position", this.position)
    ret.addProperty("readOnlyCondition", this.readOnlyCondition)
    ret.addProperty("visibleCondition", this.visibleCondition)
    ret.addProperty("property", this.property)
    ret.addProperty("helpText", this.helpText)
    if (this.defaultValue != null && this.defaultValue.length > 0) {
      if ((this.propertyType == classOf[Boolean]) || (this.propertyType == classOf[Boolean])) {
        ret.addProperty("defaultValue", this.defaultValue.toBoolean)
      }
      else if ((this.propertyType == classOf[Integer]) || (this.propertyType == classOf[Int])) {
        ret.addProperty("defaultValue", Integer.valueOf(this.defaultValue))
      }
      else {
        ret.addProperty("defaultValue", this.defaultValue)
      }
    }
    else {
      ret.addProperty("defaultValue", this.defaultValue)
    }
    val array: JsonArray = new JsonArray
    if (this.selectValues != null) {
      for (selectValue <- this.selectValues) {
        val o: JsonObject = new JsonObject
        o.addProperty("text", selectValue.getText)
        o.addProperty("value", selectValue.getValue)
        array.add(o)
      }
    }
    ret.add("selectValues", array)
    ret
  }
}
