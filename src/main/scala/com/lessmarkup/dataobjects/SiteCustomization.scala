/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.DataObject

class SiteCustomization(
  id: Long,
  @RequiredField
  var path: String,
  var isBinary: Boolean,
  @RequiredField
  var body: Array[Byte],
  @RequiredField
  var created: OffsetDateTime,
  var updated: OffsetDateTime,
  var append: Boolean
  ) extends DataObject(id)
