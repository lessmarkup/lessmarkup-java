/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.DataObject

class TestMail(
  id: Long = 0,
  @RequiredField
  var from: String,
  @RequiredField
  var to: String,
  @RequiredField
  var subject: String,
  @RequiredField
  var body: String,
  @RequiredField
  var sent: OffsetDateTime,
  var views: Int = 0,
  @RequiredField
  var template: String
  ) extends DataObject(id)
