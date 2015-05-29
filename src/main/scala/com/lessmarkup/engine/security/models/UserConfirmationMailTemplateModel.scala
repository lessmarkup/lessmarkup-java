/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security.models

import com.lessmarkup.interfaces.system.MailTemplateModel

class UserConfirmationMailTemplateModel extends MailTemplateModel {
  private[models] var link: String = null

  def getLink: String = link
  def setLink(link: String): Unit = this.link = link
}