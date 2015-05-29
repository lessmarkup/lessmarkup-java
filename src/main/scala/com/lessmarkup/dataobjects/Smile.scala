/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.DataObject

class Smile(
  id: Long,
  @RequiredField
  var contentType: String,
  @RequiredField
  var data: Array[Byte],
  @RequiredField
  var name: String,
  @RequiredField
  var code: String
  ) extends DataObject(id)
