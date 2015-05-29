/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.globalconfiguration

import com.google.gson.JsonObject
import com.google.inject.Inject
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}
import com.lessmarkup.{Constants, TextIds}
import com.lessmarkup.framework.helpers.StringHelper
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.system.SiteConfiguration
import com.lessmarkup.userinterface.model.user.LoginModel
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler

class LoginNodeHandlerFactory @Inject() (dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new LoginNodeHandler(dataCache, nodeHandlerConfiguration)
  }
}

class LoginNodeHandler (dataCache: DataCache, configuration: NodeHandlerConfiguration)
  extends DialogNodeHandler[LoginModel](dataCache, classOf[LoginModel], configuration) {

  protected def loadObject: Option[LoginModel] = None
  protected def saveObject(changedObject: Option[LoginModel]): String = null

  override def getViewData: Option[JsonObject] = {
    val ret = super.getViewData
    if (ret.isDefined) {
      val siteConfiguration: SiteConfiguration = dataCache.get(classOf[SiteConfiguration])
      val adminLoginPage: String = siteConfiguration.adminLoginPage
      ret.get.addProperty("administratorKey", if (StringHelper.isNullOrEmpty(adminLoginPage)) Constants.NodePathAdminLoginDefaultPage else adminLoginPage)
    }
    ret
  }

  override def getViewType: String = "login"

  protected override def getApplyCaption: String = TextIds.LOGIN
}