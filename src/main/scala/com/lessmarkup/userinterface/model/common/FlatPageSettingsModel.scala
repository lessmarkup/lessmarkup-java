/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.common

import com.lessmarkup.TextIds
import com.lessmarkup.interfaces.annotations.{InputField, InputFieldType}
import com.lessmarkup.interfaces.recordmodel.RecordModel
import com.lessmarkup.userinterface.model.common.FlatPagePosition.FlatPagePosition

class FlatPageSettingsModel extends RecordModel[FlatPageSettingsModel](TextIds.FLAT_PAGE_SETTINGS) {
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.LOAD_ON_SHOW, defaultValue = "false")
  var loadOnShow: Boolean = false
  @InputField(fieldType = InputFieldType.SELECT, textId = TextIds.POSITION, defaultValue = "RIGHT")
  var position: FlatPagePosition = null
  @InputField(fieldType = InputFieldType.NUMBER, textId = TextIds.LEVEL_TO_LOAD, defaultValue = "2")
  var levelToLoad: Int = 0
}