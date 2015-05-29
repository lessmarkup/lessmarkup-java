/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.{OptionString, DataObject, OptionLong}

class Node(
  id: Long = 0,
  @RequiredField
  var path: String,
  @RequiredField
  var title: String,
  @RequiredField
  var description: String,
  @RequiredField
  var handlerId: String,
  var settings: OptionString,
  var enabled: Boolean,
  var addToMenu: Boolean,
  var position: Int,
  var parentId: OptionLong
  ) extends DataObject(id)
