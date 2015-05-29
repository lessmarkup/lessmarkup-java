/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.module

import com.lessmarkup.interfaces.annotations.Implements
import com.lessmarkup.interfaces.data.DataObject
import com.lessmarkup.interfaces.module._

import scala.collection.JavaConversions._
import scala.collection.mutable

@Implements(classOf[ModuleIntegration]) class ModuleIntegrationImpl extends ModuleIntegration {
  private final val entitySearch: mutable.Map[Class[_], EntitySearch] = mutable.Map()
  private final val userPropertyProviders: mutable.ListBuffer[UserPropertyProvider] = mutable.ListBuffer()
  private var registerindModuleType: String = null
  private final val moduleActionHandlers: mutable.Map[String, ModuleActionHandler] = mutable.Map()

  def registerBackgroundJobHandler(handler: BackgroundJobHandler) {
  }

  def doBackgroundJobs(): Boolean = {
    true
  }

  def registerEntitySearch(`type`: Class[_ <: DataObject], entitySearch: EntitySearch) {
    this.entitySearch.put(`type`, entitySearch)
  }

  def registerUserPropertyProvider(provider: UserPropertyProvider) {
    userPropertyProviders.add(provider)
  }

  def getEntitySearch(collectionType: Class[_ <: DataObject]): EntitySearch = {
    entitySearch.get(collectionType).get
  }

  def getUserProperties(userId: Long): Seq[UserProperty] = {
    userPropertyProviders.flatten(provider => provider.getProperties(userId))
  }

  def getRegisteringModuleType: String = {
    registerindModuleType
  }

  def setRegisteringModuleType(value: String) {
    registerindModuleType = value
  }

  def registerActionHandler(name: String, handler: ModuleActionHandler) {
    moduleActionHandlers.put(name, handler)
  }

  def getActionHandler(name: String): ModuleActionHandler = {
    moduleActionHandlers.get(name).get
  }
}