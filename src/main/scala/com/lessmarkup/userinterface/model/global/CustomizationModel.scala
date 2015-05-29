/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.global

import com.lessmarkup.TextIds
import com.lessmarkup.dataobjects.SiteCustomization
import com.lessmarkup.framework.data.RecordModelWithEditableCollection
import com.lessmarkup.interfaces.annotations.{InputField, InputFieldType, RecordColumn}
import com.lessmarkup.interfaces.recordmodel.InputFile

class CustomizationModel extends RecordModelWithEditableCollection[CustomizationModel, SiteCustomization](TextIds.EDIT_CUSTOMIZATION, classOf[SiteCustomization], classOf[CustomizationModel]) {

  @InputField(fieldType = InputFieldType.HIDDEN, defaultValue = "false")
  var typeDefined: Boolean = false
  var body: Array[Byte] = null
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.IS_BINARY, visibleCondition = "!typeDefined")
  @RecordColumn(textId = TextIds.IS_BINARY)
  var binary: Boolean = false
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.PATH, required = true)
  @RecordColumn(textId = TextIds.PATH)
  var path: String = null
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.APPEND, defaultValue = "false", visibleCondition = "!binary")
  var append: Boolean = false
  @InputField(fieldType = InputFieldType.FILE, textId = TextIds.FILE, visibleCondition = "binary", required = true)
  def file_$eq(file: InputFile) {
    if (file != null && file.getFile != null && file.getFile.length > 0) {
      body = file.getFile
    }
  }

  def file: InputFile = {
    if (!binary) {
      return null
    }
    val ret: InputFile = new InputFile
    ret.setFile(body)
    ret.setName("File.bin")
    ret.setType("binary")
    ret
  }

  @InputField(fieldType = InputFieldType.CODE_TEXT, textId = TextIds.TEXT, visibleCondition = "!binary", required = true)
  def text_$eq(text: String) {
    body = text.getBytes
  }

  def text: String = {
    if (binary || body == null) {
      return null
    }
    new String(body)
  }
}
