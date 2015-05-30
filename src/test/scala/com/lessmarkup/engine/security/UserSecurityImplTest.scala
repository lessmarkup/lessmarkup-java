/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.security

import com.lessmarkup.Constants
import com.lessmarkup.dataobjects.UserGroup
import com.lessmarkup.engine.testutilities.WithEnvironmentSpec
import com.lessmarkup.interfaces.system.SiteConfiguration
import org.scalamock.scalatest.MockFactory
import org.scalatest._

class UserSecurityImplTest extends WithEnvironmentSpec with Matchers with MockFactory with BeforeAndAfterEach {

  "generateSalt" should "generate random bytes N length" in {
    val generatedSalt = UserSecurityImpl.generateSaltBytes
    generatedSalt should not be null
    generatedSalt.length should equal (Constants.EncryptSaltLength)
  }

  "encodePassword" should "return the same count of chars and hashed contents" in {
    val encoded1 = UserSecurityImpl.encodePassword("passw1", "salt1")
    val encoded2 = UserSecurityImpl.encodePassword("password2", "s2")
    val encoded3 = UserSecurityImpl.encodePassword("passw1", "salt1")
    encoded1.length should be (encoded2.length)
    encoded1 should be (encoded3)
    encoded1 should not be encoded2
  }

  "toHexString" should "return hexadecimal equivalent of binary data" in {
    val hexString = UserSecurityImpl.toHexString(Array(0xfa.toByte, 0x87.toByte))
    hexString should be ("fa87")
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
    val siteConfiguration = mock[SiteConfiguration]
    val defaultGroupName = "TestGrou"
    (siteConfiguration.defaultUserGroup _).expects().returns(defaultGroupName)
    dataCache.set(classOf[SiteConfiguration], None, siteConfiguration)
    domainModelProvider.create.query.from(classOf[UserGroup]).count should be (0)
    val group = userSecurity.getOrCreateDefaultGroup(domainModelProvider.create)
    domainModelProvider.create.query.from(classOf[UserGroup]).count should be (1)
    group.get.name should be (defaultGroupName)
    val groups: Seq[UserGroup] = domainModelProvider.create.query.from(classOf[UserGroup]).toList(classOf[UserGroup])
    groups.length should be (1)
    groups.head.name should be (defaultGroupName)
  }
}
