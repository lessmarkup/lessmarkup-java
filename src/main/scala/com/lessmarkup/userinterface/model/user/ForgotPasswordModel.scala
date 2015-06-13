/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.user

import java.util.Random

import com.google.inject.Inject
import com.lessmarkup.dataobjects.User
import com.lessmarkup.framework.helpers.{LanguageHelper, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.{InputField, InputFieldType}
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.recordmodel.RecordModel
import com.lessmarkup.interfaces.security.UserSecurity
import com.lessmarkup.interfaces.structure.NodeHandler
import com.lessmarkup.interfaces.system.{MailSender, SiteConfiguration}
import com.lessmarkup.{Constants, TextIds}

class ForgotPasswordModel @Inject() (userSecurity: UserSecurity, dataCache: DataCache, domainModelProvider: DomainModelProvider, mailSender: MailSender) extends RecordModel[ForgotPasswordModel](TextIds.FORGOT_PASSWORD, true) {

  private var message: String = LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.FORGOT_PASSWORD_MESSAGE)
  private var email: String = null

  def getMessage: String = {
    this.message
  }

  @InputField(fieldType = InputFieldType.LABEL) def setMessage(message: String) {
    this.message = message
  }

  def getEmail: String = {
    this.email
  }

  @InputField(fieldType = InputFieldType.EMAIL, textId = TextIds.EMAIL) def setEmail(email: String) {
    this.email = email
  }

  def submit(nodeHandler: NodeHandler, fullPath: String) {
    val siteName: String = dataCache.get(classOf[SiteConfiguration]).siteName
    if (StringHelper.isNullOrEmpty(siteName)) {
      return
    }
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val user = domainModel.query.from(classOf[User]).where("email = $", email).first(classOf[User], None)
      if (user.isEmpty) {
        try {
          Thread.sleep(new Random(System.currentTimeMillis).nextInt(400) + 100)
          return
        }
        catch {
          case e: InterruptedException =>
            return
        }
      }
      val resetUrl: String = String.format("%s/%s/%s", RequestContextHolder.getContext.getBasePath, Constants.ModuleActionsChangePassword, userSecurity.createPasswordChangeToken(user.get.id))
      val model: ResetPasswordEmailModel = new ResetPasswordEmailModel
      model.setSiteName(siteName)
      model.setResetUrl(resetUrl)
      mailSender.sendEmailWithUserIds(classOf[ResetPasswordEmailModel], None, Option(user.get.id), email, Constants.MailTemplatesResetPassword, model)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }
}