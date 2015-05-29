/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.system

trait EngineConfiguration {
  def isSafeMode: Boolean

  def setSafeMode(safeMode: Boolean)

  def getFatalErrorsEmail: String

  def setFatalErrorsEmail(email: String)

  def isSmtpConfigured: Boolean

  def getSmtpServer: String

  def setSmtpServer(server: String)

  def getSmtpUsername: String

  def setSmtpUsername(name: String)

  def getSmtpPassword: String

  def setSmtpPassword(password: String)

  def isSmtpSsl: Boolean

  def setSmtpSsl(ssl: Boolean)

  def getRecaptchaPublicKey: String

  def setRecaptchaPublicKey(key: String)

  def getRecaptchaPrivateKey: String

  def setRecaptchaPrivateKey(key: String)

  def isUseTestMail: Boolean

  def setUseTestMail(use: Boolean)

  def getNoReplyEmail: String

  def setNoReplyEmail(email: String)

  def getNoReplyName: String

  def setNoReplyName(name: String)

  def getFailedAttemptsRememberMinutes: Int

  def setFailedAttemptsRememberMinutes(minutes: Int)

  def getMaximumFailedAttempts: Int

  def setMaximumFailedAttempts(attempts: Int)

  def getRecordsPerPage: Int

  def setRecordsPerPage(recordsPerPage: Int)

  def getAuthCookieName: String

  def setAuthCookieName(name: String)

  def getAuthCookieTimeout: Int

  def setAuthCookieTimeout(timeout: Int)

  def getAuthCookiePath: String

  def setAuthCookiePath(path: String)

  def isAutoRefresh: Boolean

  def setAutoRefresh(autoRefresh: Boolean)

  def getNoAdminName: String

  def setNoAdminName(noAdminName: String)

  def getBackgroundJobInterval: Int

  def setBackgroundJobInterval(interval: Int)

  def getAdminLoginPage: String

  def setAdminLoginPage(adminLoginPage: String)

  def getAdminLoginAddress: String

  def setAdminLoginAddress(adminLoginAddress: String)

  def isMigrateDataLossAllowed: Boolean

  def setMigrateDataLossAllowed(migrateDataLossAllowed: Boolean)

  def isCustomizationsDisabled: Boolean

  def setCustomizationsDisabled(customizationsDisabled: Boolean)

  def getConnectionString: String

  def setConnectionString(connectionString: String)

  def getModulesPath: String

  def setModulesPath(modulesPath: String)

  def getSessionKey: String

  def setSessionKey(sessionKey: String)
}