/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.{DataObject, OptionLong}

class EntityChangeHistory(
  id: Long = 0,
  var userId: OptionLong,
  var entityId: Long,
  var collectionId: Int,
  var changeType: Int,
  @RequiredField
  var created: OffsetDateTime,
  var parameter1: Long = 0,
  var parameter2: Long = 0,
  var parameter3: Long = 0
  ) extends DataObject(id)
