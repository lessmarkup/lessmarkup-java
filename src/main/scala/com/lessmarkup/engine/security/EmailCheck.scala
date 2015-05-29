/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security

object EmailCheck {
  private val PATTERN: String = "^[a-z][a-z|0-9|]*([_][a-z|0-9]+)*([.][a-z|0-9]+([_][a-z|0-9]+)*)?@[a-z][a-z|0-9|]*\\.([a-z][a-z|0-9]*(\\.[a-z][a-z|0-9]*)?)$"
  private val MAXIMUM_EMAIL_LENGTH: Int = 100

  def isValidEmail(email: String): Boolean = {

    if (email == null) {
      return false
    }
    val emailTrim = email.trim
    if (emailTrim.length == 0) {
      return false
    }
    if (emailTrim.length > MAXIMUM_EMAIL_LENGTH) {
      return false
    }

    emailTrim.matches(PATTERN)
  }
}