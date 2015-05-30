/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.{DataObject, OptionLong}

class Menu extends DataObject {
  @RequiredField
  var text: String = null
  @RequiredField
  var description: String = null
  var argument: String = null
  var uniqueId: String = null
  var order: Int = 0
  var visible: Boolean = false
  @RequiredField
  var created: OffsetDateTime = null
  var updated: OffsetDateTime = null
  var imageId: OptionLong = None
}
