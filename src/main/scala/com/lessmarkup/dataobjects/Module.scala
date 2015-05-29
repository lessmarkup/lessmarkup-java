/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.dataobjects

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.DataObject

class Module(
  id: Long = 0,
  @RequiredField
  var name: String,
  @RequiredField
  var path: String,
  var enabled: Boolean,
  var removed: Boolean,
  var system: Boolean,
  @RequiredField
  var moduleType: String
  ) extends DataObject(id)