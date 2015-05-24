/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security

import java.security.MessageDigest
import java.sql.SQLException
import java.time.OffsetDateTime
import java.util.OptionalLong
import java.util.logging.Level
import javax.crypto.Cipher
import javax.servlet.http.Cookie

import com.google.gson.{JsonElement, JsonObject, JsonParser}
import com.google.inject.Inject
import com.lessmarkup.{Constants, TextIds}
import com.lessmarkup.dataobjects.{FailedLoginHistory, SuccessfulLoginHistory, User, UserLoginIpAddress}
import com.lessmarkup.framework.helpers.{DependencyResolver, LanguageHelper, LoggingHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.{DataCache, EntityChangeType}
import com.lessmarkup.interfaces.data.{ChangeTracker, DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.exceptions.DatabaseException
import com.lessmarkup.interfaces.module.Implements
import com.lessmarkup.interfaces.security.{CurrentUser, LoginTicket, UserSecurity}
import com.lessmarkup.interfaces.system.{EngineConfiguration, RequestContext, SiteConfiguration, UserCache}
import org.apache.commons.net.util.Base64

class CookieUserModel(val userId: Long, val email: String, val name: String, val properties: Option[String],
                      val groups: List[Long], val administrator: Boolean, val approved: Boolean,
                      val fakeUser: Boolean, val emailConfirmed: Boolean)

object CurrentUserImpl {

  private def generateFakeSalt(hashAlgorithm: MessageDigest, email: String): Option[String] = {

    hashAlgorithm.update(email.getBytes)
    val cipher: Cipher = UserSecurityImpl.initializeCipher(DependencyResolver.resolve(classOf[DataCache]), Cipher.ENCRYPT_MODE)
    if (cipher == null) {
      return null
    }
    var encryptedBytes: Array[Byte] = null
    try {
      val hash: Array[Byte] = hashAlgorithm.digest
      encryptedBytes = cipher.doFinal(hash)
    }
    catch {
      case ex: Any =>
        LoggingHelper.getLogger(classOf[CurrentUserImpl]).log(Level.SEVERE, null, ex)
        return None
    }

    Option(Base64.encodeBase64String(encryptedBytes))
  }

  private def addSuccessfulLoginHistory(domainModel: DomainModel, userId: Long) {

    val requestContext: RequestContext = RequestContextHolder.getContext
    val history: SuccessfulLoginHistory = new SuccessfulLoginHistory
    history.setAddress(requestContext.getRemoteAddress)
    history.setTime(OffsetDateTime.now)
    history.setUserId(userId)
    domainModel.create(history)
    val loginIpaddress: UserLoginIpAddress = new UserLoginIpAddress
    loginIpaddress.setUserId(userId)
    loginIpaddress.setCreated(OffsetDateTime.now)
    loginIpaddress.setAddress(requestContext.getRemoteAddress)
    domainModel.create(loginIpaddress)
  }
}

@Implements(classOf[CurrentUser])
class CurrentUserImpl @Inject() (domainModelProvider: DomainModelProvider, userSecurity: UserSecurity) extends CurrentUser {

  private var userData: Option[CookieUserModel] = loadCurrentUser

  private def noGlobalAdminUser(model: DomainModel): Boolean = {
    model.query
      .from(classOf[User])
      .where("administrator = $ AND removed = $ AND (blocked = $ OR unblockTime < $)", true, false, false, OffsetDateTime.now)
      .first(classOf[User], Option(Constants.DataIdPropertyName))
      .isEmpty
  }

  private def loadCurrentUser: Option[CookieUserModel] = {

    val context: RequestContext = RequestContextHolder.getContext
    val engineConfiguration: EngineConfiguration = context.getEngineConfiguration
    val cookie: Cookie = context.getCookie(engineConfiguration.getAuthCookieName)
    if (cookie == null) {
      return None
    }
    val cookieValue: String = cookie.getValue.replace('_', '/').replace('-', '+')
    val ticket: LoginTicket = userSecurity.decryptLoginTicket(cookieValue).get
    loadCurrentUser(ticket)
  }

  private def loadCurrentUser(ticket: LoginTicket): Option[CookieUserModel] = {

    if (ticket == null) {
      return None
    }

    val context: RequestContext = RequestContextHolder.getContext
    val engineConfiguration: EngineConfiguration = context.getEngineConfiguration

    if (ticket.getUserId == -1 && (ticket.getEmail == engineConfiguration.getNoAdminName)) {
      val domainModel: DomainModel = domainModelProvider.create
      try {
        if (noGlobalAdminUser(domainModel)) {
          val model: CookieUserModel = new CookieUserModel(
            email = ticket.getEmail,
            name = ticket.getName,
            administrator = true,
            fakeUser = true,
            approved = true,
            emailConfirmed = true,
            userId = -1,
            properties = None,
            groups = List()
          )
          return Option(model)
        }
      }
      catch {
        case ex: SQLException =>
          LoggingHelper.getLogger(classOf[CurrentUserImpl]).log(Level.SEVERE, null, ex)
          return None
      } finally {
        domainModel.close()
      }
    }

    val dataCache: DataCache = DependencyResolver.resolve(classOf[DataCache])
    val currentUser: UserCache = dataCache.get(classOf[UserCache], Option(ticket.getUserId))
    if (currentUser.isRemoved) {
      LoggingHelper.getLogger(getClass).info("Cannot find user " + ticket.getUserId + " for current user")
    }
    if (currentUser.isBlocked) {
      if (currentUser.getUnblockTime.isEmpty || currentUser.getUnblockTime.get.isAfter(OffsetDateTime.now)) {
        LoggingHelper.getLogger(getClass).info("User is blocked")
        return None
      }
    }
    if (!currentUser.isAdministrator && !dataCache.get(classOf[SiteConfiguration]).getHasUsers) {
      LoggingHelper.getLogger(getClass).info("User functionality is disabled by configuration")
      return None
    }

    if (!ticket.isPersistent) {
      val encryptedTicket: String = userSecurity.encryptLoginTicket(ticket)
      val cookie: Cookie = new Cookie(engineConfiguration.getAuthCookieName, encryptedTicket)
      cookie.setPath(engineConfiguration.getAuthCookiePath)
      cookie.setMaxAge(engineConfiguration.getAuthCookieTimeout * 60)
      cookie.setHttpOnly(true)
      context.setCookie(cookie)
    }

    Option(new CookieUserModel(
      email = ticket.getEmail,
      name = ticket.getName,
      groups = currentUser.getGroups,
      administrator = currentUser.isAdministrator,
      approved = currentUser.isApproved,
      emailConfirmed = currentUser.isEmailConfirmed,
      userId = ticket.getUserId,
      properties = Option(currentUser.getProperties),
      fakeUser = false
    ))
  }

  def getUserId: Option[Long] = if (userData.isDefined) Option(userData.get.userId) else None

  def getGroups: Option[List[Long]] = if (userData.isDefined) Option(userData.get.groups) else None

  def getProperties: Option[JsonObject] = {
    if (userData.isEmpty || userData.get.properties.isEmpty) {
      None
    } else {
      val parser: JsonParser = new JsonParser
      val element: JsonElement = parser.parse(userData.get.properties.get)
      if (element.isJsonObject) {
        Option(element.getAsJsonObject)
      } else {
        None
      }
    }
  }

  def isAdministrator = userData.isDefined && userData.get.administrator

  def isApproved = userData.isDefined && userData.get.approved

  def isFakeUser = userData.isDefined && userData.get.fakeUser

  def emailConfirmed = userData.isDefined && userData.get.emailConfirmed

  def getEmail: Option[String] = if (userData.isEmpty) None else Option(userData.get.email)

  def getUserName: Option[String] = if (userData.isEmpty) None else Option(userData.get.name)

  def logout() {
    val context: RequestContext = RequestContextHolder.getContext
    val configuration: EngineConfiguration = context.getEngineConfiguration
    val cookie: Cookie = new Cookie(configuration.getAuthCookieName, "")
    cookie.setMaxAge(0)
    cookie.setHttpOnly(true)
    cookie.setPath(configuration.getAuthCookiePath)
    context.setCookie(cookie)
    userData = None
  }

  def refresh() {
    userData = loadCurrentUser
  }

  private def loginUser(email: String, name: String, userId: Long, savePassword: Boolean): Boolean = {

    LoggingHelper.getLogger(getClass).info("Logging in user " + email)

    val ticket: LoginTicket = new LoginTicket
    ticket.setEmail(email)
    ticket.setName(name)
    ticket.setUserId(userId)
    ticket.setPersistent(savePassword)

    var encryptedTicket: String = userSecurity.encryptLoginTicket(ticket)
    encryptedTicket = encryptedTicket.trim.replace('+', '-').replace('/', '_')

    val context: RequestContext = RequestContextHolder.getContext
    val configuration: EngineConfiguration = context.getEngineConfiguration
    val cookie: Cookie = new Cookie(configuration.getAuthCookieName, encryptedTicket)
    cookie.setPath(configuration.getAuthCookiePath)
    cookie.setHttpOnly(true)
    if (!savePassword) {
      cookie.setMaxAge(configuration.getAuthCookieTimeout * 60)
    }
    context.setCookie(cookie)
    userData = loadCurrentUser(ticket)
    true
  }

  def loginWithPassword(email: String, password: String, savePassword: Boolean, allowAdmin: Boolean, allowRegular: Boolean, encodedPassword: String): Boolean = {

    LoggingHelper.getLogger(getClass).info("Validating user '" + email + "'")

    val dataCache: DataCache = DependencyResolver.resolve(classOf[DataCache])
    if (!allowAdmin && !dataCache.get(classOf[SiteConfiguration]).getHasUsers) {
      LoggingHelper.getLogger(getClass).info("Users functionality is disabled")
      return false
    }
    if (!EmailCheck.isValidEmail(email)) {
      LoggingHelper.getLogger(getClass).info("User '" + email + "' has invalid email")
      return false
    }
    if ((encodedPassword == null || encodedPassword.length == 0) && !TextValidator.checkPassword(password)) {
      LoggingHelper.getLogger(getClass).info("Failed to pass password rules check")
      return false
    }

    val requestContext: RequestContext = RequestContextHolder.getContext
    val engineConfiguration: EngineConfiguration = requestContext.getEngineConfiguration
    val model: DomainModel = domainModelProvider.create

    try {
      if (allowAdmin && (email == engineConfiguration.getNoAdminName) && noGlobalAdminUser(model)) {
        LoggingHelper.getLogger(getClass).info("No admin defined and user email is equal to NoAdminName")
        if (!loginUser(email, email, -1, savePassword)) {
          return false
        }
        val history: SuccessfulLoginHistory = new SuccessfulLoginHistory
        history.setAddress(requestContext.getRemoteAddress)
        history.setUserId(-2)
        history.setTime(OffsetDateTime.now)
        model.create(history)
        return true
      }

      val optionUser: Option[User] = model.query
        .from(classOf[User])
        .where("email = $", email)
        .first(classOf[User], None)

      if (optionUser.isDefined && optionUser.get.isBlocked) {
        LoggingHelper.getLogger(getClass).info("User is blocked")
        if (optionUser.get.getUnblockTime == null || optionUser.get.getUnblockTime.isAfter(OffsetDateTime.now)) {
          return false
        }
        LoggingHelper.getLogger(getClass).info("Unblock time is arrived, unblocking the user")
        optionUser.get.setBlocked(false)
        optionUser.get.setBlockReason(null)
        optionUser.get.setUnblockTime(null)
        model.update(optionUser.get)
        DependencyResolver.resolve(classOf[ChangeTracker]).addChange(classOf[User], optionUser.get.getId, EntityChangeType.UPDATED, model)
      }

      if (optionUser.isEmpty) {
        LoggingHelper.getLogger(getClass).info("Cannot find user '" + email + "'")
        return false
      }

      val user = optionUser.get

      if (!checkPassword(Option(user.getId), user.getPassword, user.getSalt, user.isBlocked, user.isRemoved, user.getRegistrationExpires,
          password, encodedPassword)) {
        LoggingHelper.getLogger(getClass).info("User '" + email + "' failed password check")
        return false
      }

      if (user.isAdministrator) {
        if (!allowAdmin) {
          LoggingHelper.getLogger(getClass).info("Expected admin but the user is not admin")
          return false
        }
      }
      else {
        if (!allowRegular) {
          LoggingHelper.getLogger(getClass).info("Expected regular user but the user is admin")
          return false
        }
      }
      if (!loginUser(email, user.getName, user.getId, savePassword)) {
        return false
      }
      CurrentUserImpl.addSuccessfulLoginHistory(model, user.getId)
      model.update(user)
      true
    }
    catch {
      case ex: SQLException =>
        LoggingHelper.getLogger(getClass).log(Level.SEVERE, null, ex)
        false
    } finally {
      if (model != null) model.close()
    }
  }

  def loginWithOAuth(provider: String, providerUserId: String, savePassword: Boolean, allowAdmin: Boolean, allowRegular: Boolean): Boolean = {

    LoggingHelper.getLogger(getClass).info("Validating OAuth user")
    val dataCache: DataCache = DependencyResolver.resolve(classOf[DataCache])
    if (!allowAdmin && !dataCache.get(classOf[SiteConfiguration]).getHasUsers) {
      LoggingHelper.getLogger(getClass).info("Users functionality is disabled")
      return false
    }
    val model: DomainModel = domainModelProvider.create
    try {
      var user: Option[User] = model.query
        .from(classOf[User])
        .where("authProvider = $ AND authProviderUserId = $", provider, providerUserId)
        .first(classOf[User], None)
      if (user.isDefined && user.get.isBlocked) {
        if (user.get.getUnblockTime != null && user.get.getUnblockTime.isBefore(OffsetDateTime.now)) {
          user.get.setBlocked(false)
          user.get.setBlockReason(null)
          user.get.setUnblockTime(null)
          model.update(user.get)
          DependencyResolver.resolve(classOf[ChangeTracker]).addChange(classOf[User], user.get.getId, EntityChangeType.UPDATED, model)
        }
        else {
          user = None
        }
      }

      if (user.isEmpty) {
        LoggingHelper.getLogger(getClass).info("Cannot find valid user for authprovider '" + provider + "' and userid '" + providerUserId + "'")
        return false
      }

      if (user.get.isAdministrator) {
        if (!allowAdmin) {
          LoggingHelper.getLogger(getClass).info("User not administrator, cancelling login")
          return false
        }
      }
      else {
        if (!allowRegular) {
          LoggingHelper.getLogger(getClass).info("User is not administrator, cancelling login")
          return false
        }
      }
      if (!loginUser(user.get.getEmail, user.get.getName, user.get.getId, savePassword)) {
        return false
      }
      CurrentUserImpl.addSuccessfulLoginHistory(model, user.get.getId)
      true
    }
    catch {
      case ex: SQLException =>
        LoggingHelper.getLogger(getClass).log(Level.SEVERE, null, ex)
        false
    } finally {
      if (model != null) model.close()
    }
  }

  def deleteSelf(password: String) {

    if (userData.isEmpty) {
      throw new Exception("Cannot find user")
    }

    val model: DomainModel = domainModelProvider.create
    try {
      val user: Option[User] = model.query
        .from(classOf[User])
        .where(Constants.DataIdPropertyName + " = $ AND removed = $", userData.get.userId, false)
        .first(classOf[User], None)
      if (user.isEmpty) {
        throw new Exception("Cannot find user")
      }
      if (!checkPassword(Option(user.get.getId), user.get.getPassword, user.get.getSalt, isBlocked = false, isRemoved = false, null, password, null)) {
        throw new Exception(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.INVALID_PASSWORD))
      }
      user.get.setRemoved(true)
      model.update(user.get)
      logout()
    } finally {
      if (model != null) model.close()
    }
  }

  def checkPassword(domainModel: DomainModel, password: String): Boolean = {

    if (userData.isEmpty) {
      return false
    }

    val user: Option[User] = domainModel.query
      .from(classOf[User])
      .where(Constants.DataIdPropertyName + " = $ AND removed = $", userData.get.userId, false)
      .first(classOf[User], None)

    user.isDefined &&
      checkPassword(Option(user.get.getId), user.get.getPassword, user.get.getSalt, user.get.isBlocked, user.get.isRemoved, user.get.getRegistrationExpires, password, null)
  }

  def getLoginHash(email: String): (String, String) = {

    val emailTrimmed = email.trim

    var hash1: String = ""
    val hash2: String = UserSecurityImpl.generateSalt

    if (email.length > 0) {
      val domainModel: DomainModel = domainModelProvider.create
      try {
        val user: Option[User] = domainModel.query
          .from(classOf[User])
          .where("email = $ AND removed = $", emailTrimmed, false)
          .first(classOf[User], Option("salt"))
        if (user.isDefined) {
          hash1 = user.get.getSalt
        }
      } finally {
        if (domainModel != null) domainModel.close()
      }
    }

    val digest: MessageDigest = MessageDigest.getInstance(Constants.EncryptHashProvider)
    if (hash1.length == 0) {
      hash1 = CurrentUserImpl.generateFakeSalt(digest, email).get
    }

    (hash1, hash2)
  }

  def checkPassword(userId: Option[Long], userPassword: String, userSalt: String, isBlocked: Boolean, isRemoved: Boolean, registrationExpires: OffsetDateTime, password: String, encodedPassword: String): Boolean = {

    if (userId.isDefined && (isBlocked || isRemoved)) {
      LoggingHelper.getLogger(getClass).info("User is null or blocked or removed")
      return false
    }

    val requestContext: RequestContext = RequestContextHolder.getContext
    val engineConfiguration: EngineConfiguration = requestContext.getEngineConfiguration
    val remoteAddress: String = requestContext.getRemoteAddress
    if (remoteAddress == null || remoteAddress.length == 0) {
      LoggingHelper.getLogger(getClass).info("User remote address is not specified")
      return false
    }
    val timeLimit: OffsetDateTime = OffsetDateTime.now.minusMinutes(engineConfiguration.getFailedAttemptsRememberMinutes)
    val maxAttemptCount: Int = engineConfiguration.getMaximumFailedAttempts * 2

    val model: DomainModel = domainModelProvider.create
    try {
      if (userId.isEmpty) {
        LoggingHelper.getLogger(getClass).info("User is not found, logging failed attempt from address '" + remoteAddress + "'")
        val failedAttempt: FailedLoginHistory = new FailedLoginHistory
        failedAttempt.setCreated(OffsetDateTime.now)
        failedAttempt.setAddress(remoteAddress)
        model.create(failedAttempt)
        return false
      }

      var attemptCount: Int = model.query
        .from(classOf[FailedLoginHistory])
        .where("userId IS NULL AND address = $ AND created > $", remoteAddress, timeLimit)
        .count

      if (attemptCount >= maxAttemptCount) {
        LoggingHelper.getLogger(getClass).info("User is exceeded failed attempt limit for remote address '" + remoteAddress + "'")
        val failedAttempt: FailedLoginHistory = new FailedLoginHistory
        failedAttempt.setCreated(OffsetDateTime.now)
        failedAttempt.setAddress(remoteAddress)
        model.create(failedAttempt)
        return false
      }

      if (registrationExpires != null && registrationExpires.isBefore(OffsetDateTime.now)) {
        LoggingHelper.getLogger(getClass).info("User registration is expired, removing the user from users list")
        val u: User = model.query.from(classOf[User]).where(Constants.DataIdPropertyName + " = $", userId.get).first(classOf[User], None).get
        u.setRemoved(true)
        model.update(u)
        return false
      }

      attemptCount = model.query.from(classOf[FailedLoginHistory]).where("userId = $ AND created > $", userId.get, timeLimit).count
      if (attemptCount >= maxAttemptCount) {
        LoggingHelper.getLogger(getClass).info("Found failed attempts for specified user which exceed maximum attempt count")
        val failedAttempt: FailedLoginHistory = new FailedLoginHistory
        failedAttempt.setCreated(OffsetDateTime.now)
        failedAttempt.setAddress(remoteAddress)
        failedAttempt.setUserId(if (userId.isDefined) OptionalLong.of(userId.get) else OptionalLong.empty())
        model.create(failedAttempt)
        return false
      }

      var passwordValid: Boolean = ("-" == userPassword) && ("-" == userSalt)
      if (!passwordValid) {
        if (encodedPassword != null && encodedPassword.length > 0) {
          val split: Array[String] = encodedPassword.split(";")
          if (split.length == 2 && split(0).trim.length > 0 && split(1).trim.length > 0) {
            val pass2: String = UserSecurityImpl.encodePassword(userPassword, split(0))
            passwordValid = pass2 == split(1)
          }
        }
        else {
          if (UserSecurityImpl.encodePassword(password, userSalt) == userPassword) {
            passwordValid = true
          }
        }
      }

      if (passwordValid) {
        LoggingHelper.getLogger(getClass).info("Password is recognized as valid")
        if (attemptCount > 0) {
          model.query.deleteFrom(classOf[FailedLoginHistory], "userId = $ AND created > $", userId.get, timeLimit)
        }
        if (registrationExpires != null) {
          val u = model.query.from(classOf[User]).where(Constants.DataIdPropertyName + " = $", userId.get).first(classOf[User], None).get
          u.setRegistrationExpires(null)
          model.update(u)
        }
        return true
      }

      LoggingHelper.getLogger(getClass).info("Password is invalid, logging new failed attempt for the user")
      val failedAttempt: FailedLoginHistory = new FailedLoginHistory
      failedAttempt.setAddress(remoteAddress)
      failedAttempt.setUserId(if (userId.isDefined) OptionalLong.of(userId.get) else OptionalLong.empty())
      failedAttempt.setCreated(OffsetDateTime.now)
      model.create(failedAttempt)

      false
    }
    catch {
      case ex: DatabaseException =>
        LoggingHelper.getLogger(getClass).log(Level.SEVERE, null, ex)
        false
    } finally {
      if (model != null) model.close()
    }
  }
}