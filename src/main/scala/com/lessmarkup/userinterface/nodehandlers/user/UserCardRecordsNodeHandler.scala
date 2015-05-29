package com.lessmarkup.userinterface.nodehandlers.user

import com.google.inject.Inject
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.DomainModelProvider
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory, ChildHandlerSettings}
import com.lessmarkup.interfaces.system.UserCache
import com.lessmarkup.userinterface.model.user.UserCardModel
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler

class UserCardRecordsNodeHandlerFactory @Inject() (domainModelProvider: DomainModelProvider, dataCache: DataCache) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new UserCardRecordsNodeHandler(domainModelProvider, dataCache, nodeHandlerConfiguration)
  }
}

class UserCardRecordsNodeHandler(domainModelProvider: DomainModelProvider, dataCache: DataCache, configuration: NodeHandlerConfiguration)
  extends RecordListNodeHandler[UserCardModel](domainModelProvider, dataCache, classOf[UserCardModel], configuration) {

  override def hasChildren: Boolean = true

  override def createChildHandler(path: Seq[String]): Option[ChildHandlerSettings] = {
    if (path.isEmpty) {
      return None
    }

    val userId = path.head.toLong

    val flatPath = path.tail.mkString("/")

    val configuration = new NodeHandlerConfiguration(
      objectId = Option(userId),
      settings = None,
      accessType = getAccessType,
      path = flatPath,
      fullPath = flatPath
    )

    val handler = createChildHandler(classOf[UserCardTabsNodeHandlerFactory], configuration, userId)

    val userCache: UserCache = dataCache.get(classOf[UserCache], Option(userId))

    Option(new ChildHandlerSettings(
      handler = handler,
      id = Option(userId),
      title = userCache.getName,
      path = path.tail.mkString("/"),
      rest = Nil
    ))
  }
}