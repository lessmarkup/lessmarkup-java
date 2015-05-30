/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.DataObject

class UserBlockHistory extends DataObject {
  var userId: Long = 0
  var blockedByUserId: Long = 0
  @RequiredField
  var created: OffsetDateTime = null
  var blockedToTime: OffsetDateTime = null
  var unblocked: Boolean = false
  @RequiredField
  var reason: String = null
  @RequiredField
  var internalReason: String = null
}
