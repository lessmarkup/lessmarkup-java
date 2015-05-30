/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.testutilities

import com.lessmarkup.interfaces.system.{MailTemplateModel, MailSender}

class TestMailSender extends MailSender {
  override def sendMail[T <: MailTemplateModel](modelType: Class[T], smtpServer: String, smtpUser: String, smtpPassword: String, smtpSsl: Boolean, emailFrom: String, emailTo: String, viewPath: String, parameters: T): Unit = ???

  override def sendMail[T <: MailTemplateModel](modelType: Class[T], emailFrom: String, emailTo: String, viewPath: String, parameters: T): Unit = ???

  override def sendMail[T <: MailTemplateModel](modelType: Class[T], userIdFrom: Option[Long], userIdTo: Option[Long], userEmailTo: String, viewPath: String, parameters: T): Unit = ???

  override def sendPlainEmail(emailTo: String, subject: String, message: String): Unit = ???
}
