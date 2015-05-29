/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.system

import java.io._
import javax.servlet.ServletConfig

import com.lessmarkup.framework.helpers.LoggingHelper
import com.lessmarkup.interfaces.system.EngineConfiguration
import com.thoughtworks.xstream.XStream

import scala.collection.JavaConversions._

class CustomizationFile {
  private var entries: java.util.List[CustomizationFileEntry] = null
  def getEntries: java.util.List[CustomizationFileEntry] = entries
  def setEntries(entries: java.util.List[CustomizationFileEntry]): Unit = this.entries = entries
}

class CustomizationFileEntry {
  private var key: String = null
  private var value: String = null

  def getKey = key
  def setKey(key: String): Unit = this.key = key

  def getValue: String = value
  def setValue(value: String): Unit = this.value = value
}

class EngineConfigurationImpl(servletConfig: ServletConfig) extends EngineConfiguration {

  private final val CONFIGURATION_DIRECTORY: String = ".config"
  private final val CONFIGURATION_FILE: String = CONFIGURATION_DIRECTORY + "/engine.xml"
  private final val overrides: java.util.Map[String, String] = new java.util.HashMap[String, String]
  private var overridesInitialized: Boolean = false

  private def getXmlStream: XStream = {
    val xStream: XStream = new XStream
    xStream.alias("file", classOf[CustomizationFile])
    xStream.alias("entry", classOf[CustomizationFileEntry])
    xStream.alias("entries", classOf[java.util.LinkedList[CustomizationFileEntry]])
    xStream
  }

  private def loadOverrides() {
    if (!overridesInitialized) {
      overridesInitialized = true
      var rootPath: String = servletConfig.getServletContext.getRealPath("/")
      if (rootPath.endsWith("/") || rootPath.endsWith("\\")) {
        rootPath = rootPath.substring(0, rootPath.length - 1)
      }
      val configurationPath: String = rootPath + CONFIGURATION_FILE
      val file: File = new File(configurationPath)
      if (file.exists) {
        val reader: Reader = new FileReader(configurationPath)
        try {
          val stream: XStream = getXmlStream
          val entries: CustomizationFile = stream.fromXML(reader).asInstanceOf[CustomizationFile]
          if (entries != null && entries.getEntries != null) {
            for (entry <- entries.getEntries) {
              if (entry.getKey != null && entry.getValue != null) {
                overrides.put(entry.getKey, entry.getValue)
              }
            }
          }
        }
        catch {
          case e: IOException =>
            LoggingHelper.logException(getClass, e)
        } finally {
          if (reader != null) reader.close()
        }
      }
    }
  }

  private def saveOverrides() {
    loadOverrides()
    val customizationFile: CustomizationFile = new CustomizationFile
    val entries: java.util.List[CustomizationFileEntry] = new java.util.LinkedList[CustomizationFileEntry]
    customizationFile.setEntries(entries)
    for (source <- overrides.entrySet) {
      val target: CustomizationFileEntry = new CustomizationFileEntry
      target.setKey(source.getKey)
      target.setValue(source.getValue)
      entries.add(target)
    }
    val rootPath: String = servletConfig.getServletContext.getRealPath("/")
    new File(rootPath + CONFIGURATION_DIRECTORY).mkdirs
    val configurationPath: String = rootPath + CONFIGURATION_FILE
    val writer: Writer = new FileWriter(configurationPath)
    try {
      val xStream: XStream = getXmlStream
      xStream.toXML(customizationFile, writer)
    }
    catch {
      case e: IOException =>
        LoggingHelper.logException(getClass, e)
    } finally {
      if (writer != null) writer.close()
    }
  }

  private def getString(parameterName: String, defaultValue: String): String = {

    var value: String = null
    loadOverrides()
    value = overrides.get(parameterName)
    if (value == null) {
      value = servletConfig.getInitParameter(parameterName)
    }
    if (value == null || value.length == 0) {
      return defaultValue
    }

    value
  }

  private def setString(parameterName: String, value: String) {

    loadOverrides()
    overrides.put(parameterName, value)
    saveOverrides()
  }

  private def getBoolean(parameterName: String, defaultValue: Boolean): Boolean = {
    val value: String = getString(parameterName, null)
    if (value == null || value.length == 0) {
      return defaultValue
    }
    (value == "true") || !(value == "false") && defaultValue
  }

  private def setBoolean(parameterName: String, value: Boolean) {
    setString(parameterName, value.toString)
  }

  private def getInteger(parameterName: String, defaultValue: Int): Int = {
    val value: String = getString(parameterName, null)
    if (value == null || value.length == 0) {
      return defaultValue
    }
    value.toInt
  }

  private def setInteger(parameterName: String, value: Int) {
    setString(parameterName, Integer.toString(value))
  }

  def isSafeMode: Boolean = {
    getBoolean("safeMode", defaultValue = false)
  }

  def setSafeMode(safeMode: Boolean) {
    setBoolean("safeMode", safeMode)
  }

  def getFatalErrorsEmail: String = {
    getString("fatalErrors", null)
  }

  def setFatalErrorsEmail(email: String) {
    setString("fatalErrors", email)
  }

  def isSmtpConfigured: Boolean = {
    val smtpServer: String = getSmtpServer
    smtpServer != null && smtpServer.length > 0
  }

  def getSmtpServer: String = {
    getString("smtpServer", null)
  }

  def setSmtpServer(server: String) {
    setString("smtpServer", server)
  }

  def getSmtpUsername: String = {
    getString("smtpUsername", null)
  }

  def setSmtpUsername(name: String) {
    setString("smtpUsername", name)
  }

  def getSmtpPassword: String = {
    getString("smtpPassword", null)
  }

  def setSmtpPassword(password: String) {
    setString("smtpPassword", password)
  }

  def isSmtpSsl: Boolean = {
    getBoolean("smtpSsl", defaultValue = false)
  }

  def setSmtpSsl(ssl: Boolean) {
    setBoolean("smtpSsl", ssl)
  }

  def getRecaptchaPublicKey: String = {
    getString("recaptchaPublicKey", null)
  }

  def setRecaptchaPublicKey(key: String) {
    setString("recaptchaPublicKey", key)
  }

  def getRecaptchaPrivateKey: String = {
    getString("recaptchaPrivateKey", null)
  }

  def setRecaptchaPrivateKey(key: String) {
    setString("recaptchaPrivateKey", key)
  }

  def isUseTestMail: Boolean = {
    getBoolean("useTestMail", defaultValue = false)
  }

  def setUseTestMail(use: Boolean) {
    setBoolean("useTestMail", use)
  }

  def getNoReplyEmail: String = {
    getString("noReplyEmail", "no@reply.email")
  }

  def setNoReplyEmail(email: String) {
    setString("noReplyEmail", email)
  }

  def getNoReplyName: String = {
    getString("noReplyName", "NoReply")
  }

  def setNoReplyName(name: String) {
    setString("noReplyName", name)
  }

  def getFailedAttemptsRememberMinutes: Int = {
    getInteger("failedAttemptsRememberMinutes", 15)
  }

  def setFailedAttemptsRememberMinutes(minutes: Int) {
    setInteger("failedAttemptsRememberMinutes", minutes)
  }

  def getMaximumFailedAttempts: Int = {
    getInteger("maximumFailedAttempts", 5)
  }

  def setMaximumFailedAttempts(attempts: Int) {
    setInteger("maximumFailedAttempts", attempts)
  }

  def getRecordsPerPage: Int = {
    getInteger("recordsPerPage", 10)
  }

  def setRecordsPerPage(recordsPerPage: Int) {
    setInteger("recordsPerPage", recordsPerPage)
  }

  def getAuthCookieName: String = {
    getString("authCookieName", "LessMarkup_Auth")
  }

  def setAuthCookieName(name: String) {
    setString("authCookieName", name)
  }

  def getAuthCookieTimeout: Int = {
    getInteger("authCookieTimeout", 15)
  }

  def setAuthCookieTimeout(timeout: Int) {
    setInteger("authCookieTimeout", timeout)
  }

  def getAuthCookiePath: String = {
    getString("authCookiePath", "/")
  }

  def setAuthCookiePath(path: String) {
    setString("authCookiePath", path)
  }

  def isAutoRefresh: Boolean = {
    getBoolean("autoRefresh", defaultValue = true)
  }

  def setAutoRefresh(autoRefresh: Boolean) {
    setBoolean("autoRefresh", autoRefresh)
  }

  def getNoAdminName: String = {
    getString("noAdminName", "noadmin@noadmin.com")
  }

  def setNoAdminName(noAdminName: String) {
    setString("noAdminName", noAdminName)
  }

  def getBackgroundJobInterval: Int = {
    getInteger("backgroundJobInterval", 10)
  }

  def setBackgroundJobInterval(interval: Int) {
    setInteger("backgroundJobInterval", interval)
  }

  def getAdminLoginPage: String = {
    getString("adminLoginPage", "Login")
  }

  def setAdminLoginPage(adminLoginPage: String) {
    setString("adminLoginPage", adminLoginPage)
  }

  def getAdminLoginAddress: String = {
    getString("adminLoginAddress", null)
  }

  def setAdminLoginAddress(adminLoginAddress: String) {
    setString("adminLoginAddress", adminLoginAddress)
  }

  def isMigrateDataLossAllowed: Boolean = {
    getBoolean("migrateDataLossAllowed", defaultValue = false)
  }

  def setMigrateDataLossAllowed(migrateDataLossAllowed: Boolean) {
    setBoolean("migrateDataLossAllowed", migrateDataLossAllowed)
  }

  def isCustomizationsDisabled: Boolean = {
    getBoolean("customizationsDisabled", defaultValue = false)
  }

  def setCustomizationsDisabled(customizationsDisabled: Boolean) {
    setBoolean("customizationsDisabled", customizationsDisabled)
  }

  def getConnectionString: String = {
    getString("connectionString", null)
  }

  def setConnectionString(connectionString: String) {
    setString("connectionString", connectionString)
  }

  def getModulesPath: String = {
    getString("modulesPath", "modules")
  }

  def setModulesPath(modulesPath: String) {
    setString("modulesPath", modulesPath)
  }

  def getSessionKey: String = {
    getString("sessionKey", null)
  }

  def setSessionKey(sessionKey: String) {
    setString("sessionKey", sessionKey)
  }
}