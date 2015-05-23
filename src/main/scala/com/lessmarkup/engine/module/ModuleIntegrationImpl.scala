/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.module

import java.util.{Collection, HashMap, LinkedList, List, Map}

import com.lessmarkup.interfaces.data.DataObject
import com.lessmarkup.interfaces.module._

import scala.collection.JavaConversions._

@Implements(classOf[ModuleIntegration]) class ModuleIntegrationImpl extends ModuleIntegration {
  private final val entitySearch: Map[Class[_], EntitySearch] = new HashMap[Class[_], EntitySearch]
  private final val userPropertyProviders: List[UserPropertyProvider] = new LinkedList[UserPropertyProvider]
  private var registerindModuleType: String = null
  private final val moduleActionHandlers: Map[String, ModuleActionHandler] = new HashMap[String, ModuleActionHandler]

  def registerBackgroundJobHandler(handler: BackgroundJobHandler) {
  }

  def doBackgroundJobs: Boolean = {
    return true
  }

  def registerEntitySearch(`type`: Class[_ <: DataObject], entitySearch: EntitySearch) {
    this.entitySearch.put(`type`, entitySearch)
  }

  def registerUserPropertyProvider(provider: UserPropertyProvider) {
    userPropertyProviders.add(provider)
  }

  def getEntitySearch(collectionType: Class[_ <: DataObject]): EntitySearch = {
    return entitySearch.get(collectionType)
  }

  def getUserProperties(userId: Long): Collection[UserProperty] = {
    userPropertyProviders.flatten(provider => provider.getProperties(userId))
  }

  def getRegisteringModuleType: String = {
    return registerindModuleType
  }

  def setRegisteringModuleType(value: String) {
    registerindModuleType = value
  }

  def registerActionHandler(name: String, handler: ModuleActionHandler) {
    moduleActionHandlers.put(name, handler)
  }

  def getActionHandler(name: String): ModuleActionHandler = {
    return moduleActionHandlers.get(name)
  }
}