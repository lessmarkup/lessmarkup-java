/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security

import com.lessmarkup.Constants
import com.lessmarkup.dataobjects.{EntityChangeHistory, UserGroupMembership, User, UserGroup}
import com.lessmarkup.engine.security.UserSecurityImplTest.TestObject
import com.lessmarkup.engine.security.models.NewUserCreatedModel
import com.lessmarkup.engine.testutilities.WithEnvironmentSpec
import com.lessmarkup.interfaces.cache.EntityChangeType
import com.lessmarkup.interfaces.security.LoginTicket
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import resource._

object UserSecurityImplTest {

  class TestObject(val param1: String, val param2: String) extends java.io.Serializable

}

class UserSecurityImplTest extends WithEnvironmentSpec with Matchers with MockFactory with BeforeAndAfterEach {

  "generateSalt" should "generate random bytes N length" in {
    val generatedSalt = UserSecurityImpl.generateSaltBytes
    generatedSalt shouldNot be(null)
    generatedSalt.length should equal (Constants.EncryptSaltLength)
  }

  "encodePassword" should "return the same count of chars and hashed contents" in {
    val encoded1 = UserSecurityImpl.encodePassword("passw1", "salt1")
    val encoded2 = UserSecurityImpl.encodePassword("password2", "s2")
    val encoded3 = UserSecurityImpl.encodePassword("passw1", "salt1")
    encoded1.length shouldBe encoded2.length
    encoded1 shouldBe encoded3
    encoded1 should not be encoded2
  }

  "toHexString" should "return hexadecimal equivalent of binary data" in {
    val hexString = UserSecurityImpl.toHexString(Array(0xfa.toByte, 0x87.toByte))
    hexString shouldBe "fa87"
  }

  "changePassword" should "generate different salt and password hash each time" in new InstanceContext {
    val password1 = userSecurity.changePassword("password1")
    val password2 = userSecurity.changePassword("pwd1")
    val password3 = userSecurity.changePassword("password1")

    password1._1 should not be password2._1
    password1._1 should not be password3._1
    password1._2 should not be password2._2
    password1._2 should not be password3._2
  }

  "generatePassword" should "generate password with specified length" in new InstanceContext {
    userSecurity.generatePassword(5).length should be (5)
    userSecurity.generatePassword(1).length should be (1)
  }

  "getOrCreateDefaultGroup" should "create default group if it does not exist" in new InstanceContext {

    for {domainModel <- managed(domainModelProvider.create)} {

      val group = userSecurity.getOrCreateDefaultGroup(domainModel)

      domainModel.query.from(classOf[UserGroup]).count should be(1)
      group.get.name shouldBe defaultGroupName
      val groups: Seq[UserGroup] = domainModel.query.from(classOf[UserGroup]).toList(classOf[UserGroup])
      groups.length shouldBe 1
      groups.head.name shouldBe defaultGroupName
    }
  }

  "addToDefaultGroup" should "add specified user to configured default group" in new InstanceContext {

    for {domainModel <- managed(domainModelProvider.create)} {

      val user = new User()
      user.name = "abc"
      user.email = "def"
      domainModel.create(user)

      userSecurity.addToDefaultGroup(domainModel, user)

      val memberships = domainModel.query.from(classOf[UserGroupMembership]).toList(classOf[UserGroupMembership])
      memberships.length shouldBe 1
      memberships.head.userId shouldBe user.id

    }
  }

  "createUser" should "create new user in simple case" in new InstanceContext {

    val userName = "UserName"
    val password = "Pa$swo_rd689"
    val email = "email@email.email"

    mailSender.sendEmailWithUserIds[NewUserCreatedModel] _ expects(*, *, *, *, *, *) once()
    siteConfiguration.adminNotifyNewUsers _ stubs() returns true

    val userId = userSecurity.createUser(
      username = userName,
      password = password,
      email = email,
      preApproved = false,
      generatePassword = false
    )

    for {domainModel <- managed(domainModelProvider.create)} {
      val user = domainModel.query.from(classOf[User]).whereId(userId).first(classOf[User]).get
      user.name shouldBe userName
      user.email shouldBe email
      val changes = domainModel.query.from(classOf[EntityChangeHistory]).toList(classOf[EntityChangeHistory])
      changes.length shouldBe 1
      val change = changes.head
      change.changeType shouldBe EntityChangeType.ADDED.value
      change.entityId shouldBe user.id
    }
  }

  "create/validatePasswordChangeToken" should "set and validate password change token" in new InstanceContext {
    for {domainModel <- managed(domainModelProvider.create)} {
      val user = new User()
      user.name = "UserName"
      user.email = "Email"
      domainModel.create(user)

      userSecurity.createPasswordChangeToken(user.id)

      val userWithToken = domainModel.query.from(classOf[User]).whereId(user.id).first(classOf[User]).get

      user.passwordChangeToken.option shouldNot be(defined)
      userWithToken.passwordChangeToken.option shouldBe defined
      userWithToken.passwordChangeTokenExpires.option shouldBe defined

      val ret = userSecurity.validatePasswordChangeToken(user.email, userWithToken.passwordChangeToken.get)

      ret shouldBe Option(user.id)
    }
  }

  "loginTicket" should "be encrypted and decrypted" in new InstanceContext {
    val ticket = new LoginTicket(
      email = "Email",
      userId = 10,
      name = "Name",
      persistent = true
    )

    val encryptedTicket = userSecurity.encryptLoginTicket(ticket)
    val decryptedTicket = userSecurity.decryptLoginTicket(encryptedTicket).get

    decryptedTicket.name shouldBe ticket.name
    decryptedTicket.email shouldBe ticket.email
    decryptedTicket.userId shouldBe ticket.userId
    decryptedTicket.persistent shouldBe ticket.persistent
  }

  "encrypt/decryptObject" should "be able to decrypt encrypted object with the same contents" in new InstanceContext {
    val testObject = new TestObject(param1 = "param1", param2 = "param2")

    val encryptedObject = userSecurity.encryptObject(testObject).get
    val decryptedObject: TestObject = userSecurity.decryptObject(classOf[TestObject], encryptedObject).get

    decryptedObject.param1 shouldBe testObject.param1
    decryptedObject.param2 shouldBe testObject.param2
  }
}
