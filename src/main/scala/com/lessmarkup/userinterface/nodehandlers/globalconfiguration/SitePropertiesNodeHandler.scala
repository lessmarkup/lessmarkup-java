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
import com.lessmarkup.userinterface.model.structure.SitePropertiesModel
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler

class SitePropertiesNodeHandlerFactory @Inject() (dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new SitePropertiesNodeHandler(dataCache, nodeHandlerConfiguration)
  }
}

@ConfigurationHandler(titleTextId = TextIds.SITE_PROPERTIES)
class SitePropertiesNodeHandler (dataCache: DataCache, configuration: NodeHandlerConfiguration)
  extends DialogNodeHandler[SitePropertiesModel](dataCache, classOf[SitePropertiesModel], configuration) {

  protected def loadObject: Option[SitePropertiesModel] = {
    val ret: SitePropertiesModel = DependencyResolver.resolve(classOf[SitePropertiesModel])
    ret.initialize(null)
    Option(ret)
  }

  protected def saveObject(changedObject: Option[SitePropertiesModel]): String = {
    if (changedObject.isDefined) {
      changedObject.get.save()
    }
    null
  }
}