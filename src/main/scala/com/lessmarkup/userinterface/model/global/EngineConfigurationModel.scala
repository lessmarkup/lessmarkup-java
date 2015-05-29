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

class EngineConfigurationModel @Inject() (dataCache: DataCache) extends RecordModel[EngineConfigurationModel] {

  private val engineConfiguration: EngineConfiguration = RequestContextHolder.getContext.getEngineConfiguration

  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.SAFE_MODE)
  var safeMode: Boolean = engineConfiguration.isSafeMode
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.CONNECTION_STRING, required = true)
  var connectionString: String = engineConfiguration.getConnectionString
  @InputField(fieldType = InputFieldType.EMAIL, textId = TextIds.FATAL_ERRORS_EMAIL)
  var fatalErrorsEmail: String = engineConfiguration.getFatalErrorsEmail
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.RECAPTCHA_PUBLIC_KEY)
  var recaptchaPublicKey: String = engineConfiguration.getRecaptchaPublicKey
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.RECAPTCHA_PRIVATE_KEY)
  var recaptchaPrivateKey: String = engineConfiguration.getRecaptchaPrivateKey
  @InputField(fieldType = InputFieldType.NUMBER, textId = TextIds.RECORDS_PER_PAGE)
  var recordsPerPage: Int = engineConfiguration.getRecordsPerPage
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.AUTH_COOKIE_NAME)
  var authCookieName: String = engineConfiguration.getAuthCookieName
  @InputField(fieldType = InputFieldType.NUMBER, textId = TextIds.AUTH_COOKIE_TIMEOUT)
  var authCookieTimeout: Int = engineConfiguration.getAuthCookieTimeout
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.AUTH_COOKIE_PATH)
  var authCookiePath: String = engineConfiguration.getAuthCookiePath
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.AUTO_REFRESH)
  var autoRefresh: Boolean = engineConfiguration.isAutoRefresh
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.NO_ADMIN_NAME)
  var noAdminName: String = engineConfiguration.getNoAdminName
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.ADMIN_LOGIN_PAGE)
  var adminLoginPage: String = engineConfiguration.getAdminLoginPage
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.ADMIN_LOGIN_ADDRESS)
  var adminLoginAddress: String = engineConfiguration.getAdminLoginAddress
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.MIGRATE_DATA_LOSS_ALLOWED)
  var migrateDataLossAllowed: Boolean = engineConfiguration.isMigrateDataLossAllowed
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.DISABLE_CUSTOMIZATIONS)
  var disableCustomizations: Boolean = engineConfiguration.isCustomizationsDisabled

  def save() {
    val engineConfiguration: EngineConfiguration = RequestContextHolder.getContext.getEngineConfiguration
    engineConfiguration.setSafeMode(safeMode)
    engineConfiguration.setConnectionString(connectionString)
    engineConfiguration.setFatalErrorsEmail(fatalErrorsEmail)
    engineConfiguration.setRecaptchaPublicKey(recaptchaPublicKey)
    engineConfiguration.setRecaptchaPrivateKey(recaptchaPrivateKey)
    engineConfiguration.setRecordsPerPage(recordsPerPage)
    engineConfiguration.setAuthCookieName(authCookieName)
    engineConfiguration.setAuthCookieTimeout(authCookieTimeout)
    engineConfiguration.setAuthCookiePath(authCookiePath)
    engineConfiguration.setAutoRefresh(autoRefresh)
    engineConfiguration.setNoAdminName(noAdminName)
    engineConfiguration.setAdminLoginPage(adminLoginPage)
    engineConfiguration.setAdminLoginAddress(adminLoginAddress)
    engineConfiguration.setMigrateDataLossAllowed(migrateDataLossAllowed)
    engineConfiguration.setCustomizationsDisabled(disableCustomizations)
    dataCache.reset()
  }
}
