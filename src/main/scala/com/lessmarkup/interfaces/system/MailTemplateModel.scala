/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.system

class MailTemplateModel {
  private var subject: String = null
  private var userEmail: String = null
  private var userName: String = null

  def getSubject: String = subject

  def setSubject(subject: String) {
    this.subject = subject
  }

  def getUserEmail: String = userEmail

  def setUserEmail(userEmail: String) {
    this.userEmail = userEmail
  }

  def getUserName: String = userName

  def setUserName(userName: String) {
    this.userName = userName
  }
}