/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.configuration

import com.lessmarkup.TextIds
import com.lessmarkup.dataobjects.{UserPropertyDefinition, UserPropertyType}
import com.lessmarkup.framework.data.RecordModelWithEditableCollection
import com.lessmarkup.interfaces.annotations.{InputField, InputFieldType, RecordColumn}

class UserPropertyModel extends RecordModelWithEditableCollection[UserPropertyModel, UserPropertyDefinition](TextIds.USER_PROPERTY, classOf[UserPropertyDefinition], classOf[UserPropertyModel]) {

  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.NAME, required = true)
  @RecordColumn(textId = TextIds.NAME)
  var name: String = null

  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.TITLE, required = true)
  @RecordColumn(textId = TextIds.TITLE)
  var title: String = null

  @InputField(fieldType = InputFieldType.SELECT, textId = TextIds.TYPE, required = true)
  @RecordColumn(textId = TextIds.TYPE)
  var propertyType: UserPropertyType.type = null
}