/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.{DataObject, BinaryData, OptionLong}

class Image extends DataObject {
  @RequiredField
  var contentType: String = null
  @RequiredField
  var data: BinaryData = null
  var thumbnailContentType: String = null
  var thumbnail: BinaryData = null
  @RequiredField
  var created: OffsetDateTime = null
  var updated: OffsetDateTime = null
  var userId: OptionLong = None
  @RequiredField
  var fileName: String = null
  var height: Int = 0
  var width: Int = 0
}
