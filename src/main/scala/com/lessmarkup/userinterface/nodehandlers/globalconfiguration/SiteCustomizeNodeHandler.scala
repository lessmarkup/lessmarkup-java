/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.globalconfiguration

import com.google.inject.Inject
import com.lessmarkup.TextIds
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.annotations.ConfigurationHandler
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.DomainModelProvider
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}
import com.lessmarkup.userinterface.model.global.CustomizationModel
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler

class SiteCustomizeNodeHandlerFactory @Inject() (domainModelProvider: DomainModelProvider, dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new SiteCustomizeNodeHandler(domainModelProvider, dataCache, nodeHandlerConfiguration)
  }
}

@ConfigurationHandler(titleTextId = TextIds.CUSTOMIZE)
class SiteCustomizeNodeHandler (domainModelProvider: DomainModelProvider, dataCache: DataCache, configuration: NodeHandlerConfiguration)
  extends RecordListNodeHandler[CustomizationModel](domainModelProvider, dataCache, classOf[CustomizationModel], configuration)