/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.module

import com.lessmarkup.interfaces.data.DataObject

trait ModuleIntegration {
  def registerBackgroundJobHandler(handler: BackgroundJobHandler)
  def doBackgroundJobs(): Boolean
  def registerEntitySearch(dataType: Class[_ <: DataObject], entitySearch: EntitySearch)
  def registerUserPropertyProvider(provider: UserPropertyProvider)
  def getEntitySearch(collectionType: Class[_ <: DataObject]): EntitySearch
  def getUserProperties(userId: Long): Seq[UserProperty]
  def registerActionHandler(name: String, handler: ModuleActionHandler)
  def getActionHandler(name: String): ModuleActionHandler
}