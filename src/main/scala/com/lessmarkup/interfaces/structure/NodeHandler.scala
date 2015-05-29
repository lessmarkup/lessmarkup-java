/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.structure

import java.lang.reflect.Method

import com.google.gson.JsonObject
import com.lessmarkup.interfaces.annotations
import com.lessmarkup.interfaces.data.DomainModel

trait NodeHandler {

  def getObjectId: Option[Long]
  def getViewData: Option[JsonObject] = None
  def hasChildren: Boolean = false
  def isStatic: Boolean = false
  def createChildHandler(path: Seq[String]): Option[ChildHandlerSettings] = None
  def getStylesheets: Seq[String]
  def getTemplateId: String
  def getViewType: String
  def getScripts: Seq[String]
  def getAccessType: annotations.NodeAccessType
  def getActionHandler(name: String, data: JsonObject): Option[(AnyRef, Method)]
  def getSettingsModel: Option[Class[_]]
  def processUpdates(fromVersion: Option[Long], toVersion: Long, returnValues: JsonObject, domainModel: DomainModel, arguments: JsonObject): Boolean
}
