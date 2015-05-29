package com.lessmarkup.userinterface.nodehandlers.user

import com.google.inject.Inject
import com.lessmarkup.framework.helpers.LanguageHelper
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.annotations.UserCardHandler
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.module.ModuleProvider
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}
import com.lessmarkup.userinterface.nodehandlers.common.TabPageNodeHandler

class UserCardTabsNodeHandlerFactory @Inject() (dataCache: DataCache, moduleProvider: ModuleProvider)
  extends NodeHandlerFactory {

  override def createNodeHandler(configuration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    if (arguments.length != 1) {
      throw new IllegalArgumentException
    }

    new UserCardTabsNodeHandler(dataCache, moduleProvider, configuration, arguments.head.asInstanceOf)
  }
}

class UserCardTabsNodeHandler(dataCache: DataCache, moduleProvider: ModuleProvider, configuration: NodeHandlerConfiguration, userId: Long)
  extends TabPageNodeHandler(dataCache, configuration) {

  override def createPages: Seq[TabPageNodeHandler.TabPage] = {
    val pages: Seq[TabPageNodeHandler.TabPage] =
      for (
        module <- moduleProvider.getModules;
        handlerType <- module.getInitializer.getNodeHandlerTypes;
        handlerAttribute: UserCardHandler = handlerType.getAnnotation(classOf[UserCardHandler])
        if handlerAttribute != null
      ) yield {
        createPage(handlerType, LanguageHelper.getText(module.getModuleType, handlerAttribute.titleTextId), handlerAttribute.path)
      }

    pages ++ super.createPages
  }

  override protected def createChildHandler(handlerType: Class[_ <: NodeHandlerFactory], nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    if (handlerType == classOf[UserCardTabsNodeHandlerFactory]) {
      super.createChildHandler(handlerType, nodeHandlerConfiguration, userId)
    } else {
      super.createChildHandler(handlerType, nodeHandlerConfiguration, arguments: _*)
    }
  }
}