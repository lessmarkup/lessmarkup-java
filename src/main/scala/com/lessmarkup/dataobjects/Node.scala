/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.{OptionString, DataObject, OptionLong}

class Node extends DataObject {
  @RequiredField
  var path: String = null
  @RequiredField
  var title: String = null
  @RequiredField
  var description: String = null
  @RequiredField
  var handlerId: String = null
  var settings: OptionString = None
  var enabled: Boolean = false
  var addToMenu: Boolean = false
  var position: Int = 0
  var parentId: OptionLong = None
}
