/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.DataObject

class SuccessfulLoginHistory(
  id: Long = 0,
  var userId: Long,
  @RequiredField
  var address: String,
  @RequiredField
  var time: OffsetDateTime
  ) extends DataObject(id)
