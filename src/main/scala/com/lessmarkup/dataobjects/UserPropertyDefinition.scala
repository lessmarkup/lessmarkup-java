/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import com.lessmarkup.dataobjects.UserPropertyType.UserPropertyType
import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.DataObject

class UserPropertyDefinition extends DataObject {
  @RequiredField
  var name: String = null
  var title: String = null
  @RequiredField
  var propertyType: UserPropertyType = UserPropertyType.NOTE
  var required: Boolean = false
}
