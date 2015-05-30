/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.annotations.RequiredField
import com.lessmarkup.interfaces.data.{OptionString, OptionOffsetDateTime, DataObject, OptionLong}

class User extends DataObject {
  var removed: Boolean = false
  var blocked: Boolean = false
  var blockReason: OptionString = None
  @RequiredField
  var name: String = null
  @RequiredField
  var email: String = null
  var salt: String = null
  var password: String = null
  var passwordChangeToken: OptionString = None
  var passwordChangeTokenExpires: OptionOffsetDateTime = None
  var administrator: Boolean = false
  var registered: OffsetDateTime = null
  var approved: Boolean = false
  var emailConfirmed: Boolean = false
  var passwordAutoGenerated: Boolean = false
  var lastLogin: OffsetDateTime = null
  var lastBlock: OptionOffsetDateTime = None
  var lastPasswordChanged: OptionOffsetDateTime = None
  var lastActivity: OffsetDateTime = null
  var registrationExpires: OptionOffsetDateTime = None
  var unblockTime: OptionOffsetDateTime = None
  var lockReason: OptionString = None
  var validateSecret: OptionString = None
  var requiresPasswordReset: Boolean = false
  var avatarImageId: OptionLong = None
  var userImageId: OptionLong = None
  var title: OptionString = None
  var signature: OptionString = None
  var showEmail: Boolean = false
  var multiFactorAuthorization: Int = 0
  var authProvider: OptionString = None
  var authProviderUserId: OptionString = None
  var settings: OptionString = None
  var properties: OptionString = None
}