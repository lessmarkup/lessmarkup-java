/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.globalconfiguration

import com.google.inject.Inject
import com.lessmarkup.framework.helpers.DependencyResolver
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}
import com.lessmarkup.userinterface.model.global.DatabaseConfigurationModel
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler

class DatabaseConfigurationNodeHandlerFactory @Inject() (dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new DatabaseConfigurationNodeHandler(dataCache, nodeHandlerConfiguration)
  }
}

class DatabaseConfigurationNodeHandler(dataCache: DataCache, configuration: NodeHandlerConfiguration)
  extends DialogNodeHandler[DatabaseConfigurationModel](dataCache, classOf[DatabaseConfigurationModel], configuration) {

  protected def loadObject: Option[DatabaseConfigurationModel] = {
    val model: DatabaseConfigurationModel = DependencyResolver(classOf[DatabaseConfigurationModel])
    model.database = RequestContextHolder.getContext.getEngineConfiguration.getConnectionString
    Option(model)
  }

  protected def saveObject(changedObject: Option[DatabaseConfigurationModel]): String = {
    if (changedObject.isEmpty) {
      ""
    } else {
      changedObject.get.save
    }
  }
}