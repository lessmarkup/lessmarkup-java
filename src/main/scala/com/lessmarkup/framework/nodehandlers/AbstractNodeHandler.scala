/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.nodehandlers

import java.lang.reflect.{Method, Modifier}
import com.google.gson.JsonObject
import com.lessmarkup.framework.helpers.{DependencyResolver, StringHelper}
import com.lessmarkup.interfaces.annotations.NodeAccessType
import com.lessmarkup.interfaces.data.DomainModel
import com.lessmarkup.interfaces.structure.{NodeHandlerFactory, NodeHandler}

abstract class AbstractNodeHandler(configuration: NodeHandlerConfiguration) extends NodeHandler {

  protected def createChildHandler(handlerType: Class[_ <: NodeHandlerFactory], nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    DependencyResolver.resolve(handlerType).createNodeHandler(nodeHandlerConfiguration, arguments: _*)
  }

  def getObjectId = configuration.objectId
  def getPath = configuration.path
  def getViewType = getClass.getSimpleName
  def getTemplateId = getViewType.toLowerCase
  def getFullPath = configuration.fullPath
  def getAccessType = configuration.accessType
  def hasManageAccess = configuration.accessType == NodeAccessType.MANAGE
  def hasWriteAccess = hasManageAccess || configuration.accessType == NodeAccessType.WRITE
  def getSettingsModel: Option[Class[_]] = None
  def getScripts: Seq[String] = Nil
  def getStylesheets: Seq[String] = Seq()

  protected def getSettings[T](settingsType: Class[T]): Option[T] = {
    if (configuration.settings.isEmpty || getSettingsModel.isEmpty || !settingsType.isInstance(configuration.settings.get)) {
      return None
    }
    Option(settingsType.cast(configuration.settings.get))
  }

  def trySubmitResponse(path: String): Boolean = {
    false
  }

  def getActionHandler(name: String, data: JsonObject): Option[(AnyRef, Method)] = {
    val nameJson = StringHelper.toJsonCase(name)
    for (method <- getClass.getMethods) {
      if (Modifier.isStatic(method.getModifiers)) {
        return None
      }
      if (nameJson == method.getName) {
        return Option((this.asInstanceOf[AnyRef], method))
      }
    }
    None
  }

  def processUpdates(fromVersion: Option[Long], toVersion: Long, returnValues: JsonObject, domainModel: DomainModel, arguments: JsonObject): Boolean = {
    false
  }
}
