/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.globalconfiguration

import com.google.inject.Inject
import com.lessmarkup.TextIds
import com.lessmarkup.framework.helpers.DependencyResolver
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.annotations.ConfigurationHandler
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}
import com.lessmarkup.userinterface.model.global.EmailConfigurationModel
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler

class EmailNodeHandlerFactory @Inject() (dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new EmailNodeHandler(dataCache, nodeHandlerConfiguration)
  }
}

@ConfigurationHandler(titleTextId = TextIds.EMAIL_CONFIGURATION)
class EmailNodeHandler(dataCache: DataCache, configuration: NodeHandlerConfiguration)
  extends DialogNodeHandler[EmailConfigurationModel](dataCache, classOf[EmailConfigurationModel], configuration) {

  protected def loadObject: Option[EmailConfigurationModel] = {
    Option(DependencyResolver(classOf[EmailConfigurationModel]))
  }

  protected def saveObject(changedObject: Option[EmailConfigurationModel]): String = {
    if (changedObject.isDefined) {
      changedObject.get.save()
    }
    null
  }
}