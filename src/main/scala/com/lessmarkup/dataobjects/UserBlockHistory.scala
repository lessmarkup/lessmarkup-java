/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.DataObject

class UserBlockHistory(
  id: Long = 0,
  var userId: Long,
  var blockedByUserId: Long,
  @RequiredField
  var created: OffsetDateTime,
  var blockedToTime: OffsetDateTime,
  var unblocked: Boolean,
  @RequiredField
  var reason: String,
  @RequiredField
  var internalReason: String
  ) extends DataObject(id)
