/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.{DataObject, OptionLong}

class FailedLoginHistory(
  id: Long = 0,
  userId: OptionLong = None,
  @RequiredField
  address: String,
  @RequiredField
  created: OffsetDateTime) extends DataObject(id)
