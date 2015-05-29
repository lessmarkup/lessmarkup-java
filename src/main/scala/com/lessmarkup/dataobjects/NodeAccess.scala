/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import com.lessmarkup.interfaces.annotations.NodeAccessType
import com.lessmarkup.interfaces.data.{DataObject, OptionLong}

class NodeAccess(
  id: Long = 0,
  var nodeId: Long,
  var accessType: NodeAccessType,
  var userId: OptionLong = None,
  var groupId: OptionLong = None
  ) extends DataObject(id)
