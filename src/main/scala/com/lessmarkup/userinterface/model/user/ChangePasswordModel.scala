/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.user

import com.lessmarkup.TextIds
import com.lessmarkup.interfaces.annotations.{InputField, InputFieldType}
import com.lessmarkup.interfaces.recordmodel.RecordModel

class ChangePasswordModel extends RecordModel[ChangePasswordModel](TextIds.CHANGE_PASSWORD) {
  private var password: String = null

  @InputField(fieldType = InputFieldType.PASSWORD_REPEAT, textId = TextIds.PASSWORD) def setPassword(password: String) {
    this.password = password
  }

  def getPassword: String = {
    password
  }
}