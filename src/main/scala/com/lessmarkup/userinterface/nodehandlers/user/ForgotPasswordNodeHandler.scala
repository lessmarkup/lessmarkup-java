/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.user

import com.google.inject.Inject
import com.lessmarkup.framework.helpers.{DependencyResolver, LanguageHelper}
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.annotations.NodeAccessType
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory, ChildHandlerSettings}
import com.lessmarkup.userinterface.model.user.ForgotPasswordModel
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler
import com.lessmarkup.{Constants, TextIds}

class ForgotPasswordNodeHandlerFactory @Inject() (dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new ForgotPasswordNodeHandler(dataCache, nodeHandlerConfiguration)
  }
}

class ForgotPasswordNodeHandler (dataCache: DataCache, configuration: NodeHandlerConfiguration)
  extends DialogNodeHandler[ForgotPasswordModel](dataCache, classOf[ForgotPasswordModel], configuration) {

  protected override def getApplyCaption: String = {
    LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.RESTORE_PASSWORD)
  }

  protected def loadObject: Option[ForgotPasswordModel] = {
    Option(DependencyResolver(classOf[ForgotPasswordModel]))
  }

  protected def saveObject(changedObject: Option[ForgotPasswordModel]): String = {
    if (changedObject.isDefined) {
      changedObject.get.submit(this, getFullPath)
    }
    null
  }

  override def createChildHandler(path: Seq[String]): Option[ChildHandlerSettings] = {

    if (path.length != 3 || path.head != "ticket") {
      return None
    }

    val nodeHandlerConfiguration = new NodeHandlerConfiguration(
      objectId = None,
      settings = None,
      accessType = NodeAccessType.READ,
      path = null,
      fullPath = null
    )

    val handler: ResetPasswordNodeHandler = createChildHandler(classOf[ResetPasswordNodeHandlerFactory], nodeHandlerConfiguration, path(1), path(2)).asInstanceOf

    Option(new ChildHandlerSettings(
      handler = handler,
      id = None,
      title = "",
      path = path.mkString("/"),
      rest = Nil
    ))
  }
}