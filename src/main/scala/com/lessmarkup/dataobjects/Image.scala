/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime
import java.util.OptionalLong

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.DataObject

class Image(
  id: Long,
  @RequiredField
  var contentType: String,
  @RequiredField
  var data: Array[Byte],
  var thumbnailContentType: String,
  var thumbnail: Array[Byte],
  @RequiredField
  var created: OffsetDateTime,
  var updated: OffsetDateTime,
  var userId: OptionalLong,
  @RequiredField
  var fileName: String,
  var height: Int,
  var width: Int
  ) extends DataObject(id)
