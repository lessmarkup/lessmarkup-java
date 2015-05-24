/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException, ObjectInputStream, ObjectOutputStream, Serializable}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.{MessageDigest, NoSuchAlgorithmException, SecureRandom}
import java.sql.SQLException
import java.time.{Instant, OffsetDateTime, ZoneOffset}
import java.util.OptionalLong
import java.util.logging.Level
import javax.crypto.{Cipher, KeyGenerator, SecretKey}
import javax.crypto.spec.SecretKeySpec

import com.google.inject.Inject
import com.lessmarkup.{Constants, TextIds}
import com.lessmarkup.dataobjects.{User, UserGroup, UserGroupMembership}
import com.lessmarkup.engine.security.models.{GeneratedPasswordModel, NewUserCreatedModel, UserConfirmationMailTemplateModel}
import com.lessmarkup.framework.helpers.{LanguageHelper, LoggingHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.{DataCache, EntityChangeType}
import com.lessmarkup.interfaces.data.{ChangeTracker, DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.exceptions.{CommonException, DatabaseException, UserValidationException}
import com.lessmarkup.interfaces.module.Implements
import com.lessmarkup.interfaces.security.{EntityAccessType, LoginTicket, UserSecurity}
import com.lessmarkup.interfaces.structure.Tuple
import com.lessmarkup.interfaces.system.{EngineConfiguration, MailSender, SiteConfiguration}
import org.apache.commons.net.util.Base64

object UserSecurityImpl {
  def encodePassword(password: String, salt: String): String = {
    try {
      val digest: MessageDigest = MessageDigest.getInstance(Constants.EncryptHashProvider)
      digest.update((salt + password).getBytes(StandardCharsets.UTF_8))
      toHexString(digest.digest)
    }
    catch {
      case ex: NoSuchAlgorithmException =>
        throw new CommonException(ex)
    }
  }

  private def validateNewUserProperties(username: String, password: String, email: String, generatePassword: Boolean) {
    if (!TextValidator.checkUsername(username)) {
      throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.INVALID_USERNAME))
    }
    if (!generatePassword && !TextValidator.checkPassword(password)) {
      throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.INVALID_PASSWORD))
    }
    if (!generatePassword && !TextValidator.checkNewPassword(password)) {
      throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.PASSWORD_TOO_SIMPLE))
    }

    val trimPos = email.zipWithIndex.reverseIterator.find(c => !Character.isWhitespace(c._1) && c._1 != '.')

    val fixedEmail = if (trimPos.isEmpty) "" else email.substring(0, trimPos.get._2)

    if (!TextValidator.checkTextField(fixedEmail) || !EmailCheck.isValidEmail(fixedEmail)) {
      throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.INVALID_EMAIL, fixedEmail))
    }
  }

  private val PASSWORD_DICTIONARY: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-!?"

  def initializeCipher(dataCache: DataCache, mode: Int): Cipher = {
    val engineConfiguration: EngineConfiguration = RequestContextHolder.getContext.getEngineConfiguration
    var secretKeyText: String = engineConfiguration.getSessionKey
    var secretKey: SecretKey = null
    if (secretKeyText == null || secretKeyText.length == 0) {
      val keyGenerator: KeyGenerator = KeyGenerator.getInstance(Constants.EncryptSymmetricCipher)
      keyGenerator.init(Constants.EncryptSymmetricKeySize)
      secretKey = keyGenerator.generateKey
      secretKeyText = Base64.encodeBase64String(secretKey.getEncoded, false)
      engineConfiguration.setSessionKey(secretKeyText)
    }
    else {
      secretKey = new SecretKeySpec(Base64.decodeBase64(secretKeyText), Constants.EncryptSymmetricCipher)
    }
    val ret: Cipher = Cipher.getInstance(Constants.EncryptSymmetricCipher)
    ret.init(mode, secretKey)
    ret
  }

  private val LOGIN_TICKET_VERSION: Int = 1
  private val LOGIN_TICKET_CONTROL_WORD: Int = 12345

  private def generateSaltBytes: Array[Byte] = {
    val secureRandom: SecureRandom = new SecureRandom
    val bytes: Array[Byte] = new Array[Byte](Constants.EncryptSaltLength)
    secureRandom.nextBytes(bytes)
    bytes
  }

  def generateSalt: String = {
    Base64.encodeBase64String(generateSaltBytes, false)
  }

  private val HEX_CODES: String = "0123456789abcdef"

  def getHex(b: Byte): String = {
    "" + HEX_CODES.charAt(b >> 4) + HEX_CODES.charAt(b & 0xf)
  }

  def toHexString(values: Array[Byte]): String = {
    values.map(v => getHex(v)).mkString("")
  }
}

@Implements(classOf[UserSecurity])
class UserSecurityImpl @Inject() (domainModelProvider: DomainModelProvider, dataCache: DataCache, mailSender: MailSender, changeTracker: ChangeTracker) extends UserSecurity {

  def changePassword(password: String): (String, String) = {
    val salt: String = UserSecurityImpl.generateSalt
    val encodedPassword: String = UserSecurityImpl.encodePassword(password, salt)
    (salt, encodedPassword)
  }

  private def checkUserExistence(email: String, domainModel: DomainModel) {
    val user: Option[User] = domainModel.query
      .from(classOf[User]).where("email = $ AND isRemoved = $", email, false).first(classOf[User], Option("Id"))
    if (user.isDefined) {
      LoggingHelper.getLogger(getClass).info(String.format("User with e-mail '%s' already exists", email))
      throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.INVALID_EMAIL, email))
    }
  }

  private def createUserObject(username: String, email: String): User = {
    val user: User = new User
    user.setSalt(UserSecurityImpl.generateSalt)
    user.setEmail(email)
    user.setName(username)
    user.setRegistered(OffsetDateTime.now)
    user.setBlocked(false)
    user.setEmailConfirmed(false)
    user.setLastLogin(OffsetDateTime.now)
    user.setLastActivity(OffsetDateTime.now)
    user
  }

  def generatePassword: String = {
    generatePassword(8)
  }

  def generatePassword(passwordLength: Int): String = {
    val data: Array[Byte] = UserSecurityImpl.generateSaltBytes
    (0 until passwordLength)
      .map(i => UserSecurityImpl.PASSWORD_DICTIONARY.charAt(data(i) % UserSecurityImpl.PASSWORD_DICTIONARY.length))
      .mkString("")
  }

  private def addToDefaultGroup(domainModel: DomainModel, user: User) {
    val defaultGroup: String = dataCache.get(classOf[SiteConfiguration]).getDefaultUserGroup
    if (defaultGroup != null && defaultGroup.length > 0) {
      val optionGroup: Option[UserGroup] = domainModel.query.from(classOf[UserGroup]).where("Name = $", defaultGroup).first(classOf[UserGroup], None)
      val group = if (optionGroup.isEmpty) {
        val g = new UserGroup
        g.setName(defaultGroup)
        domainModel.create(g)
        g
      } else {
        optionGroup.get
      }
      val membership: UserGroupMembership = new UserGroupMembership
      membership.setUserId(user.getId)
      membership.setUserGroupId(group.getId)
      domainModel.create(membership)
    }
  }

  private def sendGeneratedPassword(email: String, password: String, user: User) {
    val notificationModel: GeneratedPasswordModel = new GeneratedPasswordModel
    notificationModel.setLogin(email)
    notificationModel.setPassword(password)
    notificationModel.setSiteLink("")
    notificationModel.setSiteName(dataCache.get(classOf[SiteConfiguration]).getSiteName)
    mailSender.sendMail(classOf[GeneratedPasswordModel],
      OptionalLong.empty,
      OptionalLong.of(user.getId),
      null,
      Constants.MailTemplatesPasswordGeneratedNotification,
      notificationModel)
  }

  def createUser(username: String, password: String, email: String, preApproved: Boolean, generatePassword: Boolean): Long = {
    UserSecurityImpl.validateNewUserProperties(username, password, email, generatePassword)
    var user: User = null
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      checkUserExistence(email, domainModel)
      user = createUserObject(username, email)
      if (generatePassword) {
        if (!RequestContextHolder.getContext.getEngineConfiguration.isSmtpConfigured) {
          throw new IllegalArgumentException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.SMTP_NOT_CONFIGURED))
        }
        user.setPassword(this.generatePassword)
        user.setPasswordAutoGenerated(true)
        user.setRegistrationExpires(OffsetDateTime.now.plusDays(1))
      }
      user.setPassword(UserSecurityImpl.encodePassword(password, user.getSalt))
      try {
        domainModel.create(user)
      }
      catch {
        case ex: DatabaseException =>
          val e: SQLException = ex.getCause.asInstanceOf[SQLException]
          if (e.getErrorCode == 2627 || e.getErrorCode == 2601 || e.getErrorCode == 2512) {
            LoggingHelper.getLogger(getClass).log(Level.SEVERE, null, e)
            throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.NAME_OR_EMAIL_EXISTS))
          }
          throw ex
      }
      changeTracker.addChange(classOf[User], user.getId, EntityChangeType.ADDED, domainModel)
      addToDefaultGroup(domainModel, user)
      if (generatePassword) {
        sendGeneratedPassword(email, password, user)
      }
      val siteConfiguration: SiteConfiguration = dataCache.get(classOf[SiteConfiguration])
      if (preApproved) {
        user.setEmailConfirmed(true)
        user.setApproved(true)
        domainModel.update(user)
        userNotifyCreated(user, password)
      }
      else {
        user.setValidateSecret(generateUniqueId)
        domainModel.update(user)
        if (!siteConfiguration.getAdminApprovesNewUsers) {
          user.setApproved(true)
        }
        sendConfirmationLink(user)
      }
      if (siteConfiguration.getAdminNotifyNewUsers) {
        adminNotifyNewUsers(user, domainModel)
      }
      domainModel.completeTransaction()
    } finally {
      if (domainModel != null) domainModel.close()
    }

    user.getId
  }

  def createPasswordChangeToken(userId: Long): Option[String] = {
    LoggingHelper.getLogger(getClass).log(Level.INFO, "Creating password validation token for {0}", userId)
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val optionUser: Option[User] = domainModel.query
        .from(classOf[User])
        .where(Constants.DataIdPropertyName + " = $", userId)
        .first(classOf[User], None)
      if (optionUser.isEmpty) {
        return None
      }

      val user = optionUser.get

      user.setPasswordChangeToken(generateUniqueId)
      user.setPasswordChangeTokenExpires(OffsetDateTime.now.plusMinutes(10))
      domainModel.update(user)
      Option(user.getPasswordChangeToken)

    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def validatePasswordChangeToken(email: String, token: String): Option[Long] = {
    if (token == null || token.length == 0 || email == null || email.length == 0) {
      return None
    }
    LoggingHelper.getLogger(getClass).log(Level.INFO, "Validating password change token {0}", token)
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val user: Option[User] = domainModel.query
        .from(classOf[User])
        .where("email = $ AND passwordChangeToken = $", email, token)
        .first(classOf[User], None)
      if (user.isEmpty) {
        LoggingHelper.getLogger(getClass).info("Cannot validate password - cannot find user or token")
        return None
      }
      if (user.get.getPasswordChangeTokenExpires == null || user.get.getPasswordChangeTokenExpires.isBefore(OffsetDateTime.now)) {
        LoggingHelper.getLogger(getClass).info("Password validation token is expired")
        return None
      }
      LoggingHelper.getLogger(getClass).info("Password validated ok")
      Option(user.get.getId)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  private def initializeCipher(mode: Int): Cipher = UserSecurityImpl.initializeCipher(dataCache, mode)

  def encryptLoginTicket(ticket: LoginTicket): String = {
    val cipher: Cipher = initializeCipher(Cipher.ENCRYPT_MODE)
    if (cipher == null) {
      return null
    }
    val memoryStream: ByteArrayOutputStream = new ByteArrayOutputStream
    val objectStream: ObjectOutputStream = new ObjectOutputStream(memoryStream)
    try {
      objectStream.writeInt(UserSecurityImpl.LOGIN_TICKET_CONTROL_WORD)
      objectStream.writeInt(UserSecurityImpl.LOGIN_TICKET_VERSION)
      val currentTime: OffsetDateTime = OffsetDateTime.now.plusMinutes(RequestContextHolder.getContext.getEngineConfiguration.getAuthCookieTimeout)
      objectStream.writeLong(currentTime.toInstant.toEpochMilli)
      objectStream.writeUTF(RequestContextHolder.getContext.getRemoteAddress)
      objectStream.writeLong(ticket.getUserId)
      objectStream.writeUTF(ticket.getName)
      objectStream.writeUTF(ticket.getEmail)
      objectStream.writeBoolean(ticket.isPersistent)
      objectStream.flush()
      val rawData: Array[Byte] = memoryStream.toByteArray
      val encryptedData: Array[Byte] = cipher.doFinal(rawData)
      Base64.encodeBase64String(encryptedData, false)
    }
    catch {
      case e: Any =>
        throw new CommonException(e)
    } finally {
      if (memoryStream != null) memoryStream.close()
      if (objectStream != null) objectStream.close()
    }
  }

  def decryptLoginTicket(encryptedTicket: String): Option[LoginTicket] = {
    try {
      val cipher: Cipher = initializeCipher(Cipher.DECRYPT_MODE)
      if (cipher == null) {
        return None
      }
      var binaryData: Array[Byte] = Base64.decodeBase64(encryptedTicket)
      binaryData = cipher.doFinal(binaryData)
      val memoryStream: ByteArrayInputStream = new ByteArrayInputStream(binaryData)
      val objectStream: ObjectInputStream = new ObjectInputStream(memoryStream)
      try {
        if (objectStream.readInt != UserSecurityImpl.LOGIN_TICKET_CONTROL_WORD) {
          return None
        }
        if (objectStream.readInt != UserSecurityImpl.LOGIN_TICKET_VERSION) {
          return None
        }
        val expirationTime: OffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(objectStream.readLong), ZoneOffset.UTC)
        val remoteAddress: String = objectStream.readUTF
        if (!(RequestContextHolder.getContext.getRemoteAddress == remoteAddress)) {
          return None
        }
        val ticket: LoginTicket = new LoginTicket
        ticket.setUserId(objectStream.readLong)
        ticket.setName(objectStream.readUTF)
        ticket.setEmail(objectStream.readUTF)
        ticket.setPersistent(objectStream.readBoolean)
        if (!ticket.isPersistent && expirationTime.isBefore(OffsetDateTime.now)) {
          return None
        }
        Option(ticket)
      } finally {
        if (memoryStream != null) memoryStream.close()
        if (objectStream != null) objectStream.close()
      }
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
        None
    }
  }

  class AccessToken extends Serializable {

    private var userId: OptionalLong = null
    private var collectionId: Int = 0
    private var entityId: Long = 0L
    private var accessType: EntityAccessType = null
    private var ticks: OptionalLong = null

    def getUserId: OptionalLong = userId
    def setUserId(userId: OptionalLong): Unit = this.userId = userId

    def getCollectionId: Int = collectionId
    def setCollectionId(collectionId: Int): Unit = this.collectionId = collectionId

    def getEntityId: Long = entityId
    def setEntityId(entityId: Long): Unit = this.entityId = entityId

    def getAccessType: EntityAccessType = accessType
    def setAccessType(accessType: EntityAccessType): Unit = this.accessType = accessType

    def getTicks: OptionalLong = ticks
    def setTicks(ticks: OptionalLong): Unit = this.ticks = ticks
  }

  def createAccessToken(collectionId: Int, entityId: Long, accessType: Int, userId: Option[Long], expirationTime: Option[OffsetDateTime]): String = {
    val cipher: Cipher = initializeCipher(Cipher.ENCRYPT_MODE)
    if (cipher == null) {
      return null
    }
    val memoryStream: ByteArrayOutputStream = new ByteArrayOutputStream
    val objectStream: ObjectOutputStream = new ObjectOutputStream(memoryStream)
    try {
      objectStream.writeInt(collectionId)
      objectStream.writeLong(entityId)
      objectStream.writeInt(accessType)
      objectStream.writeLong(userId.getOrElse(0))
      if (expirationTime.isEmpty) {
        objectStream.writeLong(0)
      }
      else {
        objectStream.writeLong(expirationTime.get.toInstant.toEpochMilli)
      }
      objectStream.flush()
      val rawData: Array[Byte] = memoryStream.toByteArray
      val encryptedData: Array[Byte] = cipher.doFinal(rawData)
      Base64.encodeBase64String(encryptedData, false)
    }
    catch {
      case e: Any =>
        throw new CommonException(e)
    } finally {
      if (memoryStream != null) memoryStream.close()
      if (objectStream != null) objectStream.close()
    }
  }

  def validateAccessToken(token: String, collectionId: Int, entityId: Long, accessType: Int, userId: Option[Long]): Boolean = {
    val cipher: Cipher = initializeCipher(Cipher.DECRYPT_MODE)
    if (cipher == null) {
      return false
    }
    val memoryStream: ByteArrayInputStream = new ByteArrayInputStream(Base64.decodeBase64(token))
    val objectStream: ObjectInputStream = new ObjectInputStream(memoryStream)
    try {
      val tokenCollectionId: Int = objectStream.readInt
      val tokenEntityId: Long = objectStream.readLong
      val tokenAccessType: Int = objectStream.readInt
      val tokenUserId: Long = objectStream.readLong
      val tokenExpirationTicks: Long = objectStream.readLong
      tokenAccessType match {
        case EntityAccessType.READ =>
          if ((accessType != EntityAccessType.READ && accessType != EntityAccessType.EVERYONE) || userId.isEmpty || userId.get != tokenUserId) {
            return false
          }
        case EntityAccessType.READ_WRITE =>
          if (userId.isEmpty || userId.get != tokenUserId) {
            return false
          }
        case EntityAccessType.EVERYONE =>
        case _ =>
          return false
      }
      collectionId == tokenCollectionId && entityId == tokenEntityId && !(tokenExpirationTicks != 0 && OffsetDateTime.now.toInstant.toEpochMilli > tokenExpirationTicks)
    }
    catch {
      case e: IOException =>
        LoggingHelper.getLogger(getClass).log(Level.SEVERE, null, e)
        false
    } finally {
      if (memoryStream != null) memoryStream.close()
      if (objectStream != null) objectStream.close()
    }
  }

  def generateUniqueId: String = {
    val buffer: ByteBuffer = ByteBuffer.allocate(100)
    buffer.putLong(OffsetDateTime.now.toInstant.toEpochMilli)
    buffer.put(UserSecurityImpl.generateSaltBytes)
    val builder: StringBuilder = new StringBuilder
    val array: Array[Byte] = buffer.array
    (0 until buffer.position).map(i => UserSecurityImpl.getHex(array(i))).mkString("")
  }

  private def userNotifyCreated(user: User, password: String) {
    val model: NewUserCreatedModel = new NewUserCreatedModel
    model.setEmail(user.getEmail)
    model.setPassword(password)
    model.setSiteName(dataCache.get(classOf[SiteConfiguration]).getSiteName)
    mailSender.sendMail(classOf[NewUserCreatedModel], OptionalLong.empty, OptionalLong.of(user.getId), null, Constants.MailTemplatesUserNewUserCreated, model)
  }

  private def sendConfirmationLink(user: User) {
    val path: String = String.format("%s/%s/%s", RequestContextHolder.getContext.getBasePath, Constants.ModuleActionsValidateAccount, user.getValidateSecret)
    val confirmationModel: UserConfirmationMailTemplateModel = new UserConfirmationMailTemplateModel
    confirmationModel.setLink(path)
    mailSender.sendMail(classOf[UserConfirmationMailTemplateModel], OptionalLong.empty, OptionalLong.of(user.getId), null, Constants.MailTemplatesValidateUser, confirmationModel)
  }

  def confirmUser(validateSecret: String): Option[Long] = {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      val optionUser: Option[User] = domainModel.query
        .from(classOf[User])
        .where("validateSecret = $ AND emailConfirmed = $", validateSecret, false)
        .first(classOf[User], None)
      if (optionUser.isEmpty) {
        return None
      }
      val user = optionUser.get
      user.setValidateSecret(null)
      user.setEmailConfirmed(true)
      if (!dataCache.get(classOf[SiteConfiguration]).getAdminApprovesNewUsers) {
        user.setApproved(true)
      }
      changeTracker.addChange(classOf[User], user.getId, EntityChangeType.UPDATED, domainModel)
      domainModel.update(user)
      domainModel.completeTransaction()
      Option(user.getId)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  private def adminNotifyNewUsers(user: User, domainModel: DomainModel) {
    val model = new NewUserCreatedModel
    model.setUserId(user.getId)
    model.setName(user.getName)
    model.setEmail(user.getEmail)
    domainModel.query
      .from(classOf[User])
      .where("isAdministrator = $ AND isRemoved = $ AND isBlocked = $", true, false, false)
      .toList(classOf[User], Option(Constants.DataIdPropertyName))
      .foreach(admin => {
        val modelType = classOf[NewUserCreatedModel]
        val userIdFrom = OptionalLong.empty()
        val userIdTo = OptionalLong.of(admin.getId)
        val userEmailTo: String = null
        val viewPath = Constants.MailTemplatesAdminNewUserCreated
        mailSender.sendMail(modelType, userIdFrom, userIdTo, userEmailTo, viewPath, model)
    } )
  }

  def encryptObject(obj: AnyRef): Option[String] = {
    val cipher: Cipher = initializeCipher(Cipher.ENCRYPT_MODE)
    if (cipher == null) {
      return None
    }
    val memoryStream: ByteArrayOutputStream = new ByteArrayOutputStream
    val objectStream: ObjectOutputStream = new ObjectOutputStream(memoryStream)
    try {
      objectStream.writeObject(obj)
      val rawData: Array[Byte] = memoryStream.toByteArray
      val encryptedData: Array[Byte] = cipher.doFinal(rawData)
      Option(Base64.encodeBase64String(encryptedData, false))
    }
    catch {
      case e: Any =>
        LoggingHelper.getLogger(getClass).log(Level.SEVERE, null, e)
        None
    } finally {
      if (memoryStream != null) memoryStream.close()
      if (objectStream != null) objectStream.close()
    }
  }

  def decryptObject[T](`type`: Class[T], encrypted: String): Option[T] = {
    val cipher: Cipher = initializeCipher(Cipher.DECRYPT_MODE)
    if (cipher == null) {
      return None
    }
    var data: Array[Byte] = Base64.decodeBase64(encrypted)
    try {
      data = cipher.doFinal(data)
    }
    catch {
      case ex: Any => throw new CommonException(ex)
    }
    val memoryStream: ByteArrayInputStream = new ByteArrayInputStream(data)
    val objectStream: ObjectInputStream = new ObjectInputStream(memoryStream)
    try {
      Option(objectStream.readObject.asInstanceOf[T])
    }
    catch {
      case e: Any =>
        throw new CommonException(e)
    } finally {
      if (memoryStream != null) memoryStream.close()
      if (objectStream != null) objectStream.close()
    }
  }
}