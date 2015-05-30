/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import com.lessmarkup.interfaces.data.DataObject

class UserGroupMembership extends DataObject {
  var userId: Long = 0
  var userGroupId: Long = 0
}