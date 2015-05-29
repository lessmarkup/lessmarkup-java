/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.global

import com.lessmarkup.TextIds
import com.lessmarkup.dataobjects.UserGroup
import com.lessmarkup.framework.data.RecordModelWithEditableCollection
import com.lessmarkup.interfaces.annotations.{InputField, InputFieldType, RecordColumn}

class UserGroupModel extends RecordModelWithEditableCollection[UserGroupModel, UserGroup](TextIds.GROUP, classOf[UserGroup], classOf[UserGroupModel]) {

  @RecordColumn(textId = TextIds.NAME)
  @InputField(textId = TextIds.NAME, fieldType = InputFieldType.TEXT, required = true)
  var name: String = null
  @RecordColumn(textId = TextIds.DESCRIPTION)
  @InputField(textId = TextIds.DESCRIPTION, fieldType = InputFieldType.TEXT)
  var description: String = null
}
