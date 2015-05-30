/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.dataobjects

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.DataObject

class Module extends DataObject {
  @RequiredField
  var name: String = null
  @RequiredField
  var path: String = null
  var enabled: Boolean = false
  var removed: Boolean = false
  var system: Boolean = false
  @RequiredField
  var moduleType: String = null
}