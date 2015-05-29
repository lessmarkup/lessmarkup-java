/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.user

import com.lessmarkup.interfaces.system.MailTemplateModel

class ResetPasswordEmailModel extends MailTemplateModel {
  private var resetUrl: String = null
  private var siteName: String = null

  def getResetUrl: String = resetUrl

  def setResetUrl(resetUrl: String) {
    this.resetUrl = resetUrl
  }

  def getSiteName: String = siteName

  def setSiteName(siteName: String) {
    this.siteName = siteName
  }
}