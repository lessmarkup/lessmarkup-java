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

class Menu(
  id: Long,
  @RequiredField
  var text: String,
  @RequiredField
  var description: String,
  var argument: String,
  var uniqueId: String,
  var order: Int,
  var visible: Boolean,
  @RequiredField
  var created: OffsetDateTime,
  var updated: OffsetDateTime,
  var imageId: OptionalLong
  ) extends DataObject(id)
