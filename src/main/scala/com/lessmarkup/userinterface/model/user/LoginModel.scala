/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.user

import java.util.{Objects, Random}

import com.google.gson.{JsonElement, JsonObject}
import com.google.inject.Inject
import com.lessmarkup.framework.helpers.{LanguageHelper, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.{InputField, InputFieldType}
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.exceptions.{CommonException, UnauthorizedAccessException}
import com.lessmarkup.interfaces.recordmodel.RecordModel
import com.lessmarkup.interfaces.system.SiteConfiguration
import com.lessmarkup.{Constants, TextIds}

class LoginModel @Inject() (dataCache: DataCache) extends RecordModel[LoginModel](TextIds.LOGIN) {

  private var email: String = null
  private var password: String = null
  private var remember: Boolean = false

  def handleStage1Request(data: JsonObject): JsonObject = {
    try {
      Thread.sleep(new Random(System.currentTimeMillis).nextInt(30))
    }
    catch {
      case e: InterruptedException =>
        throw new CommonException(e)
    }
    val loginHash: (String, String) = RequestContextHolder.getContext.getCurrentUser.getLoginHash(data.get("user").getAsString)
    val ret: JsonObject = new JsonObject
    ret.addProperty("pass1", loginHash._1)
    ret.addProperty("pass2", loginHash._2)
    ret
  }

  def handleStage2Request(data: JsonObject): JsonObject = {
    val email: String = data.get("user").getAsString
    val passwordHash: String = data.get("hash").getAsString
    val savePassword: Boolean = data.get("remember").getAsBoolean
    var administratorKey: String = ""
    val temp: JsonElement = data.get("administratorKey")
    if (temp != null && !temp.isJsonNull) {
      administratorKey = temp.getAsString
    }
    val siteConfiguration: SiteConfiguration = dataCache.get(classOf[SiteConfiguration])
    var adminLoginPage: String = siteConfiguration.adminLoginPage
    if (StringHelper.isNullOrEmpty(adminLoginPage)) {
      adminLoginPage = Constants.NodePathAdminLoginDefaultPage
    }
    val allowAdministrator: Boolean = Objects.equals(administratorKey, adminLoginPage)
    val allowUser: Boolean = StringHelper.isNullOrWhitespace(administratorKey)
    if (!RequestContextHolder.getContext.getCurrentUser.loginWithPassword(email, "", savePassword, allowAdministrator, allowUser, passwordHash)) {
      throw new UnauthorizedAccessException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.USER_NOT_FOUND))
    }
    val ret: JsonObject = new JsonObject
    ret.addProperty("path", if (StringHelper.isNullOrWhitespace(adminLoginPage)) "" else "/")
    ret
  }

  def handleLogout: JsonObject = {
    RequestContextHolder.getContext.getCurrentUser.logout()
    new JsonObject
  }

  @InputField(fieldType = InputFieldType.EMAIL, textId = TextIds.EMAIL, required = true, position = 1) def setEmail(email: String) {
    this.email = email
  }

  def getEmail = email

  @InputField(fieldType = InputFieldType.PASSWORD, textId = TextIds.PASSWORD, required = true, position = 2) def setPassword(password: String) {
    this.password = password
  }

  def getPassword = password

  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.REMEMBER_ME, position = 3) def setRemember(remember: Boolean) {
    this.remember = remember
  }

  def isRemember = remember
}