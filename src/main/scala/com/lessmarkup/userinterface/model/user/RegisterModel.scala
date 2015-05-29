/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.user

import com.google.gson.{JsonElement, JsonObject}
import com.google.inject.Inject
import com.lessmarkup.{Constants, TextIds}
import com.lessmarkup.framework.helpers.{JsonSerializer, LanguageHelper, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.{InputField, InputFieldType}
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.exceptions.CommonException
import com.lessmarkup.interfaces.recordmodel.{RecordModel, RecordModelCache, RecordModelDefinition}
import com.lessmarkup.interfaces.security.UserSecurity
import com.lessmarkup.interfaces.system.SiteConfiguration

class RegisterModel @Inject() (dataCache: DataCache, userSecurity: UserSecurity) extends RecordModel[RegisterModel] {
  private var email: String = null
  private var name: String = null
  private var generatePassword: Boolean = false
  private var password: String = null
  private var showUserAgreement: Boolean = false
  private var userAgreement: String = null
  private var agree: Boolean = false

  def getRegisterObject: JsonElement = {
    val siteProperties: SiteConfiguration = dataCache.get(classOf[SiteConfiguration])
    if (!siteProperties.hasUsers) {
      throw new CommonException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.CANNOT_REGISTER_NEW_USER))
    }
    userAgreement = siteProperties.userAgreement
    showUserAgreement = !StringHelper.isNullOrWhitespace(siteProperties.userAgreement)
    val modelCache: RecordModelCache = dataCache.get(classOf[RecordModelCache])
    val ret: JsonObject = new JsonObject
    ret.add("registerObject", JsonSerializer.serializePojoToTree(this))
    ret.addProperty("modelId", modelCache.getDefinition(classOf[RegisterModel]).get.getId)
    ret
  }

  def register: JsonElement = {
    val siteProperties: SiteConfiguration = dataCache.get(classOf[SiteConfiguration])
    if (!siteProperties.hasUsers) {
      throw new CommonException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.CANNOT_REGISTER_NEW_USER))
    }
    val modelCache: RecordModelCache = dataCache.get(classOf[RecordModelCache])
    val definition: RecordModelDefinition = modelCache.getDefinition(classOf[RegisterModel]).get
    definition.validateInput(JsonSerializer.serializePojoToTree(this), isNew = true)
    userSecurity.createUser(name, password, email, preApproved = false, generatePassword = false)
    val loggedIn: Boolean = RequestContextHolder.getContext.getCurrentUser.loginWithPassword(email, password, savePassword = false, allowAdmin = false, allowRegular = true, null)
    val ret: JsonObject = new JsonObject
    ret.addProperty("userName", name)
    ret.addProperty("showConfiguration", false)
    ret.addProperty("userLoggedIn", loggedIn)
    ret
  }

  @InputField(fieldType = InputFieldType.EMAIL, textId = TextIds.EMAIL, required = true) def setEmail(email: String) {
    this.email = email
  }

  def getEmail: String = {
    email
  }

  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.USER_NAME, required = true) def setName(name: String) {
    this.name = name
  }

  def getName: String = {
    name
  }

  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.GENERATE_PASSWORD) def setGeneratePassword(generatePassword: Boolean) {
    this.generatePassword = generatePassword
  }

  def isGeneratePassword: Boolean = {
    generatePassword
  }

  @InputField(fieldType = InputFieldType.PASSWORD_REPEAT, textId = TextIds.PASSWORD, required = true, visibleCondition = "!generatePassword") def setPassword(password: String) {
    this.password = password
  }

  def getPassword: String = {
    password
  }

  def setShowUserAgreement(showUserAgreement: Boolean) {
    this.showUserAgreement = showUserAgreement
  }

  def isShowUserAgreement: Boolean = {
    showUserAgreement
  }

  @InputField(fieldType = InputFieldType.RICH_TEXT, textId = TextIds.USER_AGREEMENT, visibleCondition = "showUserAgreement") def setUserAgreement(userAgreement: String) {
    this.userAgreement = userAgreement
  }

  def getUserAgreement: String = {
    userAgreement
  }

  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.AGREE, required = true, visibleCondition = "showUserAgreement") def setAgree(agree: Boolean) {
    this.agree = agree
  }

  def isAgree: Boolean = {
    agree
  }
}