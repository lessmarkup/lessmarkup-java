/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.testutilities

import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.lessmarkup.engine.data.ChangeTrackerImpl
import com.lessmarkup.engine.security.{CurrentUserImpl, UserSecurityImpl}
import com.lessmarkup.engine.system.RequestContextImpl

abstract class WithEnvironmentSpec extends DatabaseFactory {

  private lazy val staticEnvironment = StaticTestEnvironmentHolder.testEnvironment

  trait InstanceContext {
    lazy val domainModelProvider = staticEnvironment.domainModelProvider
    lazy val moduleProvider = staticEnvironment.moduleProvider
    lazy val changeTracker = new ChangeTrackerImpl(domainModelProvider)
    lazy val dataCache = new TestDataCache
    lazy val mailSender = new TestMailSender
    lazy val userSecurity = new UserSecurityImpl(domainModelProvider, dataCache, mailSender, changeTracker)
    lazy val currentUser = new CurrentUserImpl(domainModelProvider, userSecurity)
    private lazy val servletRequest = mock[HttpServletRequest]
    private lazy val servletResponse = mock[HttpServletResponse]
    private lazy val servletConfig = mock[ServletConfig]
    lazy val requestContext = new RequestContextImpl(servletRequest, servletResponse, servletConfig)
  }
}
