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
import java.util.logging.Level
import javax.crypto.spec.SecretKeySpec
import javax.crypto.{Cipher, KeyGenerator, SecretKey}

import com.google.inject.Inject
import com.lessmarkup.dataobjects.{User, UserGroup, UserGroupMembership}
import com.lessmarkup.engine.security.models.{GeneratedPasswordModel, NewUserCreatedModel, UserConfirmationMailTemplateModel}
import com.lessmarkup.framework.helpers.{LanguageHelper, LoggingHelper, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.Implements
import com.lessmarkup.interfaces.cache.{DataCache, EntityChangeType}
import com.lessmarkup.interfaces.data.{ChangeTracker, DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.exceptions.{CommonException, DatabaseException, UserValidationException}
import com.lessmarkup.interfaces.security.EntityAccessType._
import com.lessmarkup.interfaces.security.{EntityAccessType, LoginTicket, UserSecurity}
import com.lessmarkup.interfaces.system.{EngineConfiguration, MailSender, SiteConfiguration}
import com.lessmarkup.{Constants, TextIds}
import org.apache.commons.net.util.Base64
import resource._

object UserSecurityImpl {
  private val PASSWORD_DICTIONARY: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-!?"
  private val LOGIN_TICKET_VERSION: Int = 1
  private val LOGIN_TICKET_CONTROL_WORD: Int = 12345
  private val HEX_CODES: String = "0123456789abcdef"

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

  def toHexString(values: Array[Byte]): String = {
    values.map(v => getHex(v.toInt)).mkString("")
  }

  private def getHex(b: Integer): String = {
    "" + HEX_CODES.charAt((b >> 4) & 0xF) + HEX_CODES.charAt(b & 0xf)
  }

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

  def generateSalt: String = {
    Base64.encodeBase64String(generateSaltBytes, false)
  }

  def generateSaltBytes: Array[Byte] = {
    val secureRandom: SecureRandom = new SecureRandom
    val bytes: Array[Byte] = new Array[Byte](Constants.EncryptSaltLength)
    secureRandom.nextBytes(bytes)
    bytes
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
}

@Implements(classOf[UserSecurity])
class UserSecurityImpl @Inject() (domainModelProvider: DomainModelProvider, dataCache: DataCache, mailSender: MailSender, changeTracker: ChangeTracker) extends UserSecurity {

  def changePassword(password: String): (String, String) = {
    val salt: String = UserSecurityImpl.generateSalt
    val encodedPassword: String = UserSecurityImpl.encodePassword(password, salt)
    (salt, encodedPassword)
  }

  def createUser(username: String, password: String, email: String, preApproved: Boolean, generatePassword: Boolean): Long = {
    UserSecurityImpl.validateNewUserProperties(username, password, email, generatePassword)
    var user: User = null

    for {domainModel <- managed(domainModelProvider.createWithTransaction)} {

      checkUserExistence(email, domainModel)

      user = createUserObject(username, email)
      if (generatePassword) {
        if (!RequestContextHolder.getContext.getEngineConfiguration.isSmtpConfigured) {
          throw new IllegalArgumentException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.SMTP_NOT_CONFIGURED))
        }
        user.password = this.generatePassword
        user.passwordAutoGenerated = true
        user.registrationExpires = Option(OffsetDateTime.now.plusDays(1))
      }
      user.password = UserSecurityImpl.encodePassword(password, user.salt)
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
      changeTracker.addChange(classOf[User], user.id, EntityChangeType.ADDED, domainModel)
      addToDefaultGroup(domainModel, user)
      if (generatePassword) {
        sendGeneratedPassword(email, password, user)
      }
      val siteConfiguration: SiteConfiguration = dataCache.get(classOf[SiteConfiguration])
      if (preApproved) {
        user.emailConfirmed = true
        user.approved = true
        domainModel.update(user)
        userNotifyCreated(user, password)
      }
      else {
        user.validateSecret = Option(generateUniqueId)
        domainModel.update(user)
        if (!siteConfiguration.adminApprovesNewUsers) {
          user.approved = true
        }
        sendConfirmationLink(user)
      }
      if (siteConfiguration.adminNotifyNewUsers) {
        adminNotifyNewUsers(user, domainModel)
      }
      domainModel.completeTransaction()
    }

    user.id
  }

  private def checkUserExistence(email: String, domainModel: DomainModel) {
    val idDecorated = domainModel.query.decorateName(Constants.DataIdPropertyName)
    val user: Option[User] = domainModel.query
      .from(classOf[User]).where("$+email = $ AND $+removed = $", email, false).first(classOf[User], Option(idDecorated))
    if (user.isDefined) {
      LoggingHelper.getLogger(getClass).info(String.format("User with e-mail '%s' already exists", email))
      throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.INVALID_EMAIL, email))
    }
  }

  private def createUserObject(username: String, email: String): User = {
    val user = new User
    user.salt = UserSecurityImpl.generateSalt
    user.email = email
    user.name = username
    user.registered = OffsetDateTime.now
    user.blocked = false
    user.emailConfirmed = false
    user.lastLogin = OffsetDateTime.now
    user.lastActivity = OffsetDateTime.now
    user
  }

  def generatePassword: String = {
    generatePassword(8)
  }

  def generatePassword(passwordLength: Int): String = {
    val data: Array[Byte] = UserSecurityImpl.generateSaltBytes
    (0 until passwordLength)
      .map(i => UserSecurityImpl.PASSWORD_DICTIONARY.charAt((data(i).toInt&0xff) % UserSecurityImpl.PASSWORD_DICTIONARY.length))
      .mkString("")
  }

  def addToDefaultGroup(domainModel: DomainModel, user: User) {
    val defaultGroup = getOrCreateDefaultGroup(domainModel)
    if (defaultGroup.isEmpty) {
      return
    }
    val membership = new UserGroupMembership
    membership.userId = user.id
    membership.userGroupId = defaultGroup.get.id
    domainModel.create(membership)
  }

  def getOrCreateDefaultGroup(domainModel: DomainModel): Option[UserGroup] = {
    val defaultGroup: String = dataCache.get(classOf[SiteConfiguration]).defaultUserGroup

    if (StringHelper.isNullOrEmpty(defaultGroup)) {
      return None
    }

    val optionGroup: Option[UserGroup] = domainModel.query.from(classOf[UserGroup]).where("$+Name = $", defaultGroup).first(classOf[UserGroup], None)
    if (optionGroup.isEmpty) {
      val g = new UserGroup
      g.name = defaultGroup
      g.description = ""
      domainModel.create(g)
      Option(g)
    } else {
      optionGroup
    }
  }

  def sendGeneratedPassword(email: String, password: String, user: User) {
    val notificationModel: GeneratedPasswordModel = new GeneratedPasswordModel
    notificationModel.setLogin(email)
    notificationModel.setPassword(password)
    notificationModel.setSiteLink("")
    notificationModel.setSiteName(dataCache.get(classOf[SiteConfiguration]).siteName)
    mailSender.sendEmailWithUserIds(classOf[GeneratedPasswordModel],
      None,
      Option(user.id),
      null,
      Constants.MailTemplatesPasswordGeneratedNotification,
      notificationModel)
  }

  def generateUniqueId: String = {
    val buffer: ByteBuffer = ByteBuffer.allocate(100)
    buffer.putLong(OffsetDateTime.now.toInstant.toEpochMilli)
    buffer.put(UserSecurityImpl.generateSaltBytes)
    val array: Array[Byte] = buffer.array
    (0 until buffer.position).map(i => UserSecurityImpl.getHex(array(i).toInt)).mkString("")
  }

  private def userNotifyCreated(user: User, password: String) {
    val model: NewUserCreatedModel = new NewUserCreatedModel
    model.setEmail(user.email)
    model.setPassword(password)
    model.setSiteName(dataCache.get(classOf[SiteConfiguration]).siteName)
    mailSender.sendEmailWithUserIds(classOf[NewUserCreatedModel], None, Option(user.id), null, Constants.MailTemplatesUserNewUserCreated, model)
  }

  private def sendConfirmationLink(user: User) {
    val path: String = String.format("%s/%s/%s", RequestContextHolder.getContext.getBasePath, Constants.ModuleActionsValidateAccount, user.validateSecret)
    val confirmationModel: UserConfirmationMailTemplateModel = new UserConfirmationMailTemplateModel
    confirmationModel.setLink(path)
    mailSender.sendEmailWithUserIds(classOf[UserConfirmationMailTemplateModel], None, Option(user.id), null, Constants.MailTemplatesValidateUser, confirmationModel)
  }

  private def adminNotifyNewUsers(user: User, domainModel: DomainModel) {
    val model = new NewUserCreatedModel
    model.setUserId(user.id)
    model.setName(user.name)
    model.setEmail(user.email)
    domainModel.query
      .from(classOf[User])
      .where("$+administrator = $ AND $+removed = $ AND $+blocked = $", true, false, false)
      .toList(classOf[User], Option(Constants.DataIdPropertyName))
      .foreach(admin => {
      val modelType = classOf[NewUserCreatedModel]
      val userIdFrom = None
      val userIdTo = Option(admin.id)
      val userEmailTo: String = null
      val viewPath = Constants.MailTemplatesAdminNewUserCreated
      mailSender.sendEmailWithUserIds(modelType, userIdFrom, userIdTo, userEmailTo, viewPath, model)
    })
  }

  def createPasswordChangeToken(userId: Long): Option[String] = {
    LoggingHelper.getLogger(getClass).log(Level.INFO, "Creating password validation token for {0}", userId)
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val optionUser: Option[User] = domainModel.query
        .from(classOf[User])
        .where("$+" + Constants.DataIdPropertyName + " = $", userId)
        .first(classOf[User], None)
      if (optionUser.isEmpty) {
        return None
      }

      val user = optionUser.get

      user.passwordChangeToken = Option(generateUniqueId)
      user.passwordChangeTokenExpires = Option(OffsetDateTime.now.plusMinutes(10))
      domainModel.update(user)
      user.passwordChangeToken

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
        .where("$+email = $ AND $+passwordChangeToken = $", email, token)
        .first(classOf[User], None)
      if (user.isEmpty) {
        LoggingHelper.getLogger(getClass).info("Cannot validate password - cannot find user or token")
        return None
      }
      if (user.get.passwordChangeTokenExpires.isEmpty || user.get.passwordChangeTokenExpires.get.isBefore(OffsetDateTime.now)) {
        LoggingHelper.getLogger(getClass).info("Password validation token is expired")
        return None
      }
      LoggingHelper.getLogger(getClass).info("Password validated ok")
      Option(user.get.id)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

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
      objectStream.writeLong(ticket.userId)
      objectStream.writeUTF(ticket.name)
      objectStream.writeUTF(ticket.email)
      objectStream.writeBoolean(ticket.persistent)
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
        val ticket: LoginTicket = new LoginTicket(
          userId = objectStream.readLong,
          name = objectStream.readUTF,
          email = objectStream.readUTF,
          persistent = objectStream.readBoolean
        )
        if (!ticket.persistent && expirationTime.isBefore(OffsetDateTime.now)) {
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

  private def initializeCipher(mode: Int): Cipher = UserSecurityImpl.initializeCipher(dataCache, mode)

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
        case READ.value =>
          if (accessType != READ.value && accessType != EVERYONE.value || userId.isEmpty || userId.get != tokenUserId) {
            return false
          }
        case READ_WRITE.value =>
          if (userId.isEmpty || userId.get != tokenUserId) {
            return false
          }
        case EVERYONE.value =>
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

  def confirmUser(validateSecret: String): Option[Long] = {
    val domainModel: DomainModel = domainModelProvider.createWithTransaction
    try {
      val optionUser: Option[User] = domainModel.query
        .from(classOf[User])
        .where("$+validateSecret = $ AND $+emailConfirmed = $", validateSecret, false)
        .first(classOf[User], None)
      if (optionUser.isEmpty) {
        return None
      }
      val user = optionUser.get
      user.validateSecret = None
      user.emailConfirmed = true
      if (!dataCache.get(classOf[SiteConfiguration]).adminApprovesNewUsers) {
        user.approved = true
      }
      changeTracker.addChange(classOf[User], user.id, EntityChangeType.UPDATED, domainModel)
      domainModel.update(user)
      domainModel.completeTransaction()
      Option(user.id)
    } finally {
      if (domainModel != null) domainModel.close()
    }
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

  class AccessToken extends Serializable {

    private var userId: Option[Long] = None
    private var collectionId: Int = 0
    private var entityId: Long = 0L
    private var accessType: EntityAccessType = null
    private var ticks: Option[Long] = None

    def getUserId: Option[Long] = userId

    def setUserId(userId: Option[Long]): Unit = this.userId = userId

    def getCollectionId: Int = collectionId

    def setCollectionId(collectionId: Int): Unit = this.collectionId = collectionId

    def getEntityId: Long = entityId

    def setEntityId(entityId: Long): Unit = this.entityId = entityId

    def getAccessType: EntityAccessType = accessType

    def setAccessType(accessType: EntityAccessType): Unit = this.accessType = accessType

    def getTicks: Option[Long] = ticks

    def setTicks(ticks: Option[Long]): Unit = this.ticks = ticks
  }
}