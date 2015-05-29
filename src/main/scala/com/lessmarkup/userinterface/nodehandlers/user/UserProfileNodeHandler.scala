package com.lessmarkup.userinterface.nodehandlers.user

import com.google.inject.Inject
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}
import com.lessmarkup.userinterface.nodehandlers.common.TabPageNodeHandler

class UserProfileNodeHandlerFactory @Inject() (dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new UserProfileNodeHandler(dataCache, nodeHandlerConfiguration)
  }
}

class UserProfileNodeHandler(dataCache: DataCache, configuration: NodeHandlerConfiguration) extends TabPageNodeHandler(dataCache, configuration)