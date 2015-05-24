/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security

object TextValidator {
  private val MaximumUsernameLength: Int = 30
  private val MaximumPasswordLength: Int = 100
  private val MaximumTextLength: Int = 128
  private val MinimumPasswordLetterOrDigit: Int = 4
  private val MinimumUsernameLength: Int = 6
  private val MinimumPasswordLength: Int = 7

  def checkPassword(text: String): Boolean = {
    checkString(text, MinimumPasswordLength, MaximumPasswordLength)
  }

  def checkNewPassword(text: String): Boolean = {

    if (!checkPassword(text)) {
      return false
    }

    val hasDigit = text.exists(_.isDigit)
    val hasUpper = text.exists(_.isUpper)
    val hasLower = text.exists(_.isLower)
    val lettersOrDigits = text.count(_.isLetterOrDigit)

    !(!hasDigit || !hasUpper || hasLower || lettersOrDigits < MinimumPasswordLetterOrDigit)
  }

  def checkUsername(text: String): Boolean = {
    checkString(text, MinimumUsernameLength, MaximumUsernameLength)
  }

  def checkTextField(text: String): Boolean = {
    checkString(text, 0, MaximumTextLength)
  }

  private def checkString(text: String, minLength: Int, maxLength: Int): Boolean = {
    if (text == null || text.length == 0) {
      return minLength == 0
    }
    if (text.length > maxLength) {
      return false
    }

    !text.exists(c => Character.isISOControl(c) || Character.isSpaceChar(c) || Character.isWhitespace(c))
  }
}