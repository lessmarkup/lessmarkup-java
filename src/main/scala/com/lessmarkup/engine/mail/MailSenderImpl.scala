/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.mail

import java.io.Writer
import java.time.OffsetDateTime
import java.util.OptionalLong
import java.util.logging.{Level, Logger}

import com.google.inject.Inject
import com.lessmarkup.dataobjects.{TestMail, User}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.module.Implements
import com.lessmarkup.interfaces.system.{EngineConfiguration, MailSender, MailTemplateModel, MailTemplateProvider, SiteConfiguration}
import org.apache.commons.net.smtp.{AuthenticatingSMTPClient, SimpleSMTPHeader}

@Implements(classOf[MailSender])
class MailSenderImpl @Inject() (domainModelProvider: DomainModelProvider, mailTemplateProvider: MailTemplateProvider, dataCache: DataCache) extends MailSender {

  private def getNoReplyEmail: String = {
    val siteConfiguration: SiteConfiguration = dataCache.get(classOf[SiteConfiguration])
    var ret: String = siteConfiguration.getNoReplyEmail
    if (ret == null || ret.length == 0) {
      ret = RequestContextHolder.getContext.getEngineConfiguration.getNoReplyEmail
      if (ret == null || ret.length == 0) {
        ret = "no@reply.email"
      }
    }
    ret
  }

  private def getNoReplyName: String = {
    val siteConfiguration: SiteConfiguration = dataCache.get(classOf[SiteConfiguration])
    var ret: String = siteConfiguration.getNoReplyName
    if (ret == null || ret.length == 0) {
      ret = RequestContextHolder.getContext.getEngineConfiguration.getNoReplyName
    }
    ret
  }

  private def composeAddress(email: String, name: String): String = {
    String.format("\"%s\" <%s>", name, email)
  }

  def sendMail[T <: MailTemplateModel](modelType: Class[T], smtpServer: String, smtpUser: String, smtpPassword: String, smtpSsl: Boolean, emailFrom: String, emailTo: String, viewPath: String, parameters: T) {
    var body: String = null
    var subject: String = null
    parameters.setUserEmail(emailTo)
    body = mailTemplateProvider.executeTemplate(modelType, viewPath, parameters)
    subject = parameters.getSubject
    sendMail("", emailFrom, parameters.getUserName, emailTo, subject, body, viewPath, smtpServer, smtpUser, smtpPassword, smtpSsl)
  }

  def sendMail[T <: MailTemplateModel](modelType: Class[T], emailFrom: String, emailTo: String, viewPath: String, parameters: T) {
    val engineConfiguration: EngineConfiguration = RequestContextHolder.getContext.getEngineConfiguration
    sendMail(modelType, engineConfiguration.getSmtpServer, engineConfiguration.getSmtpUsername, engineConfiguration.getSmtpPassword, engineConfiguration.isSmtpSsl, emailFrom, emailTo, viewPath, parameters)
  }

  def sendMail[T <: MailTemplateModel](`type`: Class[T], userIdFrom: OptionalLong, userIdTo: OptionalLong, userEmailTo: String, viewPath: String, parameters: T) {
    try {
      var userFrom: User = null
      var userTo: User = null
      val domainModel: DomainModel = domainModelProvider.create
      try {
        if (userIdTo.isPresent) {
          userTo = domainModel.query.from(classOf[User]).whereId(userIdTo.getAsLong).firstJava(classOf[User])
        }
        if (userIdFrom.isPresent) {
          userFrom = domainModel.query.from(classOf[User]).whereId(userIdFrom.getAsLong).firstJava(classOf[User])
        }
      } finally {
        if (domainModel != null) domainModel.close()
      }
      var fromName: String = null
      var fromEmail: String = null
      if (userFrom != null) {
        fromEmail = if (userFrom.isShowEmail) userFrom.getEmail else getNoReplyEmail
        fromName = userFrom.getName
      }
      else {
        fromEmail = getNoReplyEmail
        fromName = getNoReplyName
      }
      if (userTo != null) {
        parameters.setUserEmail(userTo.getEmail)
        parameters.setUserName(userTo.getName)
      }
      else if (userEmailTo != null && userEmailTo.length > 0) {
        parameters.setUserEmail(userEmailTo)
      }
      else {
        throw new IllegalArgumentException
      }
      val body: String = mailTemplateProvider.executeTemplate(`type`, viewPath, parameters)
      val subject: String = parameters.getSubject
      if (fromEmail == null) {
        throw new IllegalArgumentException
      }
      val engineConfiguration: EngineConfiguration = RequestContextHolder.getContext.getEngineConfiguration
      sendMail(fromName, fromEmail, parameters.getUserName, parameters.getUserEmail, subject, body, viewPath, engineConfiguration.getSmtpServer, engineConfiguration.getSmtpUsername, engineConfiguration.getSmtpPassword, engineConfiguration.isSmtpSsl)
    }
    catch {
      case e: Exception =>
        Logger.getLogger(classOf[MailSenderImpl].getName).log(Level.SEVERE, null, e)
    }
  }

  def sendPlainEmail(emailTo: String, subject: String, message: String) {
  }

  private def sendMail(fromName: String, fromAddress: String, toName: String, toAddress: String, subject: String, body: String, viewPath: String, server: String, username: String, password: String, useSsl: Boolean) {
    if (RequestContextHolder.getContext.getEngineConfiguration.isUseTestMail) {
      val domainModel: DomainModel = domainModelProvider.create
      try {
        val testMail: TestMail = new TestMail
        testMail.setBody(body)
        testMail.setFrom(composeAddress(fromAddress, fromName))
        testMail.setTemplate(viewPath)
        testMail.setSent(OffsetDateTime.now)
        testMail.setSubject(subject)
        testMail.setTo(composeAddress(toAddress, toName))
        testMail.setViews(0)
        domainModel.create(testMail)
      }
      catch {
        case ex: Exception =>
          Logger.getLogger(classOf[MailSenderImpl].getName).log(Level.SEVERE, null, ex)
      } finally {
        if (domainModel != null) domainModel.close()
      }
      return
    }
    if (server == null || server.length == 0) {
      throw new IllegalArgumentException
    }
    var client: AuthenticatingSMTPClient = null
    try {
      if (useSsl) {
        client = new AuthenticatingSMTPClient("TLS", true)
      }
      else {
        client = new AuthenticatingSMTPClient
      }
      client.connect(server)
      client.ehlo("localhost")
      client.auth(AuthenticatingSMTPClient.AUTH_METHOD.LOGIN, username, password)
      client.setSender(composeAddress(fromAddress, fromName))
      client.addRecipient(composeAddress(toAddress, toName))
      val writer: Writer = client.sendMessageData
      try {
        val header: SimpleSMTPHeader = new SimpleSMTPHeader(fromAddress, toAddress, subject)
        writer.write(header.toString)
        writer.write(body)
      } finally {
        if (writer != null) writer.close()
      }
      client.completePendingCommand
      client.logout()
      client.disconnect()
    }
    catch {
      case e: Exception =>
        Logger.getLogger(classOf[MailSenderImpl].getName).log(Level.SEVERE, null, e)
    }
  }
}