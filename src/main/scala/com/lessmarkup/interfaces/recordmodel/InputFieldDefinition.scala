/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.recordmodel

import com.lessmarkup.framework.helpers.{PropertyDescriptor, StringHelper}
import com.lessmarkup.interfaces.annotations
import com.lessmarkup.interfaces.annotations.{InputFieldType, InputField}

class InputFieldDefinition(property: PropertyDescriptor, definition: InputField) {
  private final val id: String =
    if (StringHelper.isNullOrEmpty(definition.id)) StringHelper.toJsonCase(property.getName)
    else definition.id

  private final val readOnly: Boolean = definition.readOnly()
  private final val `type`: annotations.InputFieldType = definition.fieldType()
  private final val readOnlyCondition: String = definition.readOnlyCondition()
  private final val required: Boolean = definition.required()
  private final val textId: String = definition.textId()
  private final val visibleCondition: String = definition.visibleCondition()
  private final val width: Double = definition.width()
  private final val minWidth: Int = definition.minWidth()
  private final val maxWidth: Int = definition.maxWidth()
  private final val position: Int = definition.position()
  private final val defaultValue: String = definition.defaultValue()
  private final val enumValues: List[InputFieldEnum] = initializeEnum()
  private final val inlineWithPrevious: Boolean = definition.inlineWithPrevious()

  private def initializeEnum(): List[InputFieldEnum] = {
    if (!(property.getType.isEnum && (`type` == InputFieldType.SELECT || `type` == InputFieldType.SELECT_TEXT || `type` == InputFieldType.MULTI_SELECT)) || definition.enumTextIdBase == null) {
      return List()
    }
    val enumType = property.getType
    val textIdBase = definition.enumTextIdBase
    enumType.getEnumConstants.map(c => {
      val enumTextId = textIdBase + c.toString
      new InputFieldEnum(enumTextId, c.toString)
    })
    .toList
  }

  def getId: String = id

  def getProperty: PropertyDescriptor = property

  def isRequired: Boolean = required

  def getType: annotations.InputFieldType = `type`

  def isReadOnly: Boolean = readOnly

  def getVisibleCondition: String = visibleCondition

  def getReadOnlyCondition: String = readOnlyCondition

  def getTextId: String = textId

  def getWidth: Double = width

  def getMinWidth: Int = minWidth

  def getMaxWidth: Int = maxWidth

  def getPosition: Int = position

  def getDefaultValue: String = defaultValue

  def getEnumValues: List[InputFieldEnum] = enumValues

  def isInlineWithPrevious: Boolean = inlineWithPrevious
}