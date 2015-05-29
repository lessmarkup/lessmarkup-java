/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.global

import com.google.inject.Inject
import com.lessmarkup.TextIds
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.{InputField, InputFieldType}
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.recordmodel.RecordModel
import com.lessmarkup.interfaces.system.EngineConfiguration

class EmailConfigurationModel @Inject() (dataCache: DataCache) extends RecordModel[EmailConfigurationModel] {

  val engineConfiguration: EngineConfiguration = RequestContextHolder.getContext.getEngineConfiguration

  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.SMTP_SERVER)
  var server: String = engineConfiguration.getSmtpServer
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.SMTP_USERNAME)
  var username: String = engineConfiguration.getSmtpUsername
  @InputField(fieldType = InputFieldType.PASSWORD, textId = TextIds.SMTP_PASSWORD)
  var password: String = engineConfiguration.getSmtpPassword
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.SMTP_USE_SSL)
  var useSsl: Boolean = engineConfiguration.isSmtpSsl
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.USE_TEST_MAIL)
  var useTestMail: Boolean = engineConfiguration.isUseTestMail
  @InputField(fieldType = InputFieldType.EMAIL, textId = TextIds.NO_REPLY_EMAIL)
  var noReplyMail: String = engineConfiguration.getNoReplyEmail
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.NO_REPLY_NAME)
  var noReplyName: String = engineConfiguration.getNoReplyName

  def save() {
    val engineConfiguration: EngineConfiguration = RequestContextHolder.getContext.getEngineConfiguration
    engineConfiguration.setSmtpServer(server)
    engineConfiguration.setSmtpUsername(username)
    engineConfiguration.setSmtpPassword(password)
    engineConfiguration.setSmtpSsl(useSsl)
    engineConfiguration.setUseTestMail(useTestMail)
    engineConfiguration.setNoReplyEmail(noReplyMail)
    engineConfiguration.setNoReplyName(noReplyName)
  }
}
