/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.{BinaryData, DataObject}

class SiteCustomization extends DataObject {
  @RequiredField
  var path: String = null
  var isBinary: Boolean = false
  @RequiredField
  var body: BinaryData = null
  @RequiredField
  var created: OffsetDateTime = null
  var updated: OffsetDateTime = null
  var append: Boolean = false
}
