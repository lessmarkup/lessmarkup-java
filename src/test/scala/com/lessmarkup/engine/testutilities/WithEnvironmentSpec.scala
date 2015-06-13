/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.testutilities

import javax.servlet.{ServletContext, ServletConfig}
import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}

import com.lessmarkup.engine.data.ChangeTrackerImpl
import com.lessmarkup.engine.security.{CurrentUserImpl, UserSecurityImpl}
import com.lessmarkup.engine.system.RequestContextImpl
import com.lessmarkup.framework.helpers.DependencyResolver
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{DomainModelProvider, ChangeTracker}
import com.lessmarkup.interfaces.security.UserSecurity
import com.lessmarkup.interfaces.system.{MailSender, SiteConfiguration}

abstract class WithEnvironmentSpec extends DatabaseFactory {

  private val staticEnvironment = StaticTestEnvironmentHolder.testEnvironment

  trait InstanceContext {

    val defaultGroupName = "TestGroup"
    val siteConfiguration = mock[SiteConfiguration]
    val mailSender = mock[MailSender]
    val domainModelProvider = staticEnvironment.domainModelProvider
    val changeTracker = new TestChangeTrackerImpl(domainModelProvider)
    val dataCache = new TestDataCache(changeTracker)
    val userSecurity = new UserSecurityImpl(domainModelProvider, dataCache, mailSender, changeTracker)
    val moduleProvider = {
      val moduleProvider = staticEnvironment.moduleProvider
      prepareSiteConfiguration(siteConfiguration)
      dataCache.set(classOf[SiteConfiguration], None, siteConfiguration)
      DependencyResolver.defineOverride(classOf[DataCache], dataCache)
      DependencyResolver.defineOverride(classOf[ChangeTracker], changeTracker)
      DependencyResolver.defineOverride(classOf[DomainModelProvider], domainModelProvider)
      DependencyResolver.defineOverride(classOf[MailSender], mailSender)
      DependencyResolver.defineOverride(classOf[UserSecurity], userSecurity)
      moduleProvider
    }
    val currentUser = {
      val servletRequest = mock[HttpServletRequest]
      val servletResponse = mock[HttpServletResponse]
      val servletConfig = mock[ServletConfig]
      val servletContext = mock[ServletContext]
      val engineConfiguration = new TestEngineConfigurationImpl(servletConfig)
      (servletConfig.getServletContext _).stubs().returns(servletContext).anyNumberOfTimes()
      (servletConfig.getInitParameter _).stubs(*).returns("").anyNumberOfTimes()
      (servletContext.getInitParameter _).stubs(*).returns("").anyNumberOfTimes()
      (servletRequest.getRemoteAddr _).stubs().returns("address").anyNumberOfTimes()
      (servletRequest.getCookies _).stubs().returns(new Array[Cookie](0)).anyNumberOfTimes()
      (servletRequest.getScheme _).stubs().returns("http").anyNumberOfTimes()
      (servletRequest.getServerName _).stubs().returns("localhost").anyNumberOfTimes()
      (servletRequest.getServerPort _).stubs().returns(80).anyNumberOfTimes()
      (servletRequest.getServletPath _).stubs().returns("/tmp/servlettemp").anyNumberOfTimes()
      (servletRequest.getContextPath _).stubs().returns("/")
      val requestContext = new RequestContextImpl(servletRequest, servletResponse, servletConfig, Option(engineConfiguration))
      RequestContextHolder.onRequestStarted(requestContext)
      new CurrentUserImpl(domainModelProvider, userSecurity)
    }

    def prepareSiteConfiguration(siteConfiguration: SiteConfiguration): Unit = {
      (siteConfiguration.defaultUserGroup _).expects().returns(defaultGroupName).anyNumberOfTimes()
      (siteConfiguration.adminApprovesNewUsers _).expects().returns(false).anyNumberOfTimes()
    }

  }
}
