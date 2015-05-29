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
import com.lessmarkup.userinterface.model.global.EngineConfigurationModel
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler

class EngineNodeHandlerFactory @Inject() (dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new EngineNodeHandler(dataCache, nodeHandlerConfiguration)
  }
}

@ConfigurationHandler(titleTextId = TextIds.ENGINE_CONFIGURATION)
class EngineNodeHandler (dataCache: DataCache, configuration: NodeHandlerConfiguration)
  extends DialogNodeHandler[EngineConfigurationModel](dataCache, classOf[EngineConfigurationModel], configuration) {

  protected def loadObject: Option[EngineConfigurationModel] = {
    Option(DependencyResolver.resolve(classOf[EngineConfigurationModel]))
  }

  protected def saveObject(changedObject: Option[EngineConfigurationModel]): String = {
    if (changedObject.isDefined) {
      changedObject.get.save()
    }
    null
  }
}