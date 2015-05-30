/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.logging.Level

import com.google.inject.Inject
import com.lessmarkup.dataobjects.{Node, NodeAccess}
import com.lessmarkup.framework.helpers._
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.NodeAccessType
import com.lessmarkup.interfaces.cache.{AbstractCacheHandler, DataCache}
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.module.ModuleProvider
import com.lessmarkup.interfaces.security.CurrentUser
import com.lessmarkup.interfaces.structure._
import com.lessmarkup.interfaces.system.SiteConfiguration
import com.lessmarkup.userinterface.nodehandlers.configuration.ConfigurationRootNodeHandlerFactory
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration.{DatabaseConfigurationNodeHandlerFactory, LoginNodeHandlerFactory}
import com.lessmarkup.userinterface.nodehandlers.user.{ForgotPasswordNodeHandlerFactory, UserCardRecordsNodeHandlerFactory, UserProfileNodeHandlerFactory}
import com.lessmarkup.userinterface.nodehandlers.{DefaultRootNodeHandler, DefaultRootNodeHandlerFactory}
import com.lessmarkup.{Constants, TextIds}

class NodeCacheImpl @Inject() (domainModelProvider: DomainModelProvider, moduleProvider: ModuleProvider, dataCache: DataCache) extends AbstractCacheHandler(Seq(classOf[Node])) with NodeCache {

  private def buildChildNode(dataNode: Node, access: Option[Seq[CachedNodeAccess]],
                             allDataNodes: Seq[Node],
                             allNodeAccess: Map[Long, Seq[CachedNodeAccess]],
                             fullPath: String): CachedNodeInformation = {

    val handler = moduleProvider.getNodeHandler(dataNode.handlerId)
    val childrenFullPath = fullPath + "/" + dataNode.path
    val childrenDataNodes = allDataNodes.filter(n => n.parentId.isDefined && n.parentId.get == dataNode.id)
    val childrenNodes = childrenDataNodes.map(n => buildChildNode(n, allNodeAccess.get(n.id), allDataNodes, allNodeAccess, childrenFullPath))
    new CachedNodeInformationImpl(dataNode, access, childrenFullPath, childrenNodes, false, handler)
  }

  private val cachedNodes: Seq[CachedNodeInformation] = {
    val domainModel: DomainModel = this.domainModelProvider.create

    try {
      val cachedNodeAccess = domainModel.query
        .from(classOf[NodeAccess])
        .toList(classOf[NodeAccess])
        .map(n => new CachedNodeAccess(n))
        .groupBy(na => na.getNodeId)

      val nodes = domainModel.query
        .from(classOf[Node])
        .toList(classOf[Node])

      val existingRoot = nodes.find(n => n.parentId.isEmpty)

      val root: Node = if (existingRoot.isEmpty) {
        val node = new Node
        node.id = 1
        node.title = "Home"
        node.description = ""
        node.settings = None
        node.handlerId = classOf[DefaultRootNodeHandler].getSimpleName
        node.enabled = true
        node.path = ""
        node
      } else {
        existingRoot.get
      }

      val rootChildren = nodes
        .filter(n => (n.parentId.isDefined && n.parentId.get == root.id) || (n.parentId.isEmpty && n.id != root.id))
        .map(c => buildChildNode(c, cachedNodeAccess.get(c.id), nodes, cachedNodeAccess, root.path))

      val configurationNode = createVirtualNode(classOf[ConfigurationRootNodeHandlerFactory], Constants.NodePathConfiguration,
        LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.CONFIGURATION), Constants.ModuleTypeMain, NodeAccessType.NO_ACCESS)

      val siteConfiguration: SiteConfiguration = this.dataCache.get(classOf[SiteConfiguration])

      val adminLoginPage = if (StringHelper.isNullOrWhitespace(siteConfiguration.adminLoginPage)) {
        val engineAdminLoginPage = RequestContextHolder.getContext.getEngineConfiguration.getAdminLoginPage
        if (StringHelper.isNullOrWhitespace(engineAdminLoginPage)) {
          Constants.NodePathAdminLoginDefaultPage
        } else {
          engineAdminLoginPage
        }
      } else {
        siteConfiguration.adminLoginPage
      }

      val adminLoginNode = createVirtualNode(classOf[LoginNodeHandlerFactory], adminLoginPage, LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.ADMINISTRATOR_LOGIN), Constants.ModuleTypeMain, NodeAccessType.READ)

      val userProfileNode = createVirtualNode(classOf[UserProfileNodeHandlerFactory], Constants.NodePathProfile, LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.USER_PROFILE), Constants.ModuleTypeMain, NodeAccessType.READ, loggedIn = true)

      val userNodes: Seq[CachedNodeInformationImpl] = if (!siteConfiguration.hasUsers) Seq() else {
        val userCardsNode = createVirtualNode(classOf[UserCardRecordsNodeHandlerFactory], Constants.NodePathUserCards, LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.USER_CARDS), Constants.ModuleTypeMain, NodeAccessType.READ)
        val forgotPasswordNode = createVirtualNode(classOf[ForgotPasswordNodeHandlerFactory], Constants.NodePathForgotPassword, LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.FORGOT_PASSWORD), Constants.ModuleTypeMain, NodeAccessType.READ)
        Seq(userCardsNode, forgotPasswordNode)
      }

      val rootChildrenWithVirtualNodes = rootChildren ++ Seq(configurationNode, adminLoginNode, userProfileNode) ++ userNodes

      val rootNode = new CachedNodeInformationImpl(root, cachedNodeAccess.get(root.id), root.path, rootChildrenWithVirtualNodes)

      def flatNodesTree(node: CachedNodeInformation): Seq[CachedNodeInformation] =
        node.children.flatMap(c => flatNodesTree(c)) :+ node

      val ret = flatNodesTree(rootNode)

      ret.foreach {
        case n: CachedNodeInformationImpl =>
          n.root = rootNode
          n.children.foreach {
            case c: CachedNodeInformationImpl =>
              c.parent = Option(n)
          }
      }

      ret

    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  private val rootNode = cachedNodes.find(n => n.parent.isEmpty)

  private val idToNode = cachedNodes.map(n => (n.nodeId, n)).toMap

  private def createVirtualNode[T <: NodeHandlerFactory](handlerType: Class[T], path: String, title: String,
    moduleType: String, accessType: NodeAccessType, loggedIn: Boolean = false): CachedNodeInformationImpl = {

    val nodeId = idToNode.keys.max

    val node = new Node
    node.id = nodeId
    node.path = path.toLowerCase
    node.title = title
    node.handlerId = handlerType.getSimpleName
    node.enabled = true
    node.description = ""
    node.settings = None
    node.addToMenu = false

    val cachedNodeAccess = new CachedNodeAccess(accessType, None, None, nodeId)

    new CachedNodeInformationImpl(node, Option(Seq(cachedNodeAccess)), path, Seq(), loggedIn, Option((handlerType, Constants.ModuleTypeMain)))
  }

  def getNode(nodeId: Long): Option[CachedNodeInformation] = idToNode.get(nodeId)

  def getNode(path: String): Option[(CachedNodeInformation, String)] = {

    val nodeParts: Seq[String] = if (StringHelper.isNullOrEmpty(path)) Seq() else path.split("/").filter(p => p.length > 0)

    if (nodeParts.isEmpty) {
      return Option(this.rootNode.get, "")
    }

    def getChildNode(node: CachedNodeInformation, path: String, parts: Seq[String]): Option[(CachedNodeInformation, Seq[String])] = {
      val child = node.children.find(n => n.path == path)
      if (child.isEmpty) {
        None
      } else {
        if (parts.isEmpty || child.get.children.isEmpty) {
          Option((child.get, parts))
        } else {
          getChildNode(child.get, parts.head, parts.tail)
        }
      }
    }

    val node = getChildNode(rootNode.get, nodeParts.head, nodeParts.tail)

    if (node.isDefined) {
      Option((node.get._1, node.get._2.mkString("/")))
    } else {
      None
    }
  }

  def getRootNode: Option[CachedNodeInformation] = {
    rootNode
  }

  def getNodes: Seq[CachedNodeInformation] = {
    cachedNodes
  }

  private def traverseParents(node: CachedNodeInformation, filter: Option[(NodeHandler, String, String, Seq[String], Option[Long]) => Boolean]): Boolean = {
    if (node.parent.isDefined) {
      if (traverseParents(node.parent.get, filter)) {
        return true
      }
    }
    if (filter.isDefined)
      false
    else
      filter.get(null, node.title, node.fullPath, null, Option(node.nodeId))
  }

  def getNodeHandler(path: String, filter: Option[(NodeHandler, String, String, Seq[String], Option[Long]) => Boolean]): Option[NodeHandler] = {

    val pathDecoded: String = if (StringHelper.isNullOrEmpty(path)) "" else {
      try {
        URLDecoder.decode(path, "UTF-8")
      }
      catch {
        case ex: UnsupportedEncodingException =>
          LoggingHelper.getLogger(getClass).log(Level.SEVERE, null, ex)
          ""
      }
    }

    val pathWithoutQuery = if (StringHelper.isNullOrEmpty(pathDecoded)) pathDecoded else {
      val queryPost: Int = pathDecoded.indexOf('?')
      if (queryPost >= 0) {
        pathDecoded.substring(0, queryPost)
      } else {
        pathDecoded
      }
    }

    val foundNode = getNode(pathWithoutQuery)

    if (foundNode.isEmpty) {
      LoggingHelper.getLogger(getClass).log(Level.INFO, "Cannot get node for path ''{0}''", path)
      return None
    }

    val node = foundNode.get._1

    val currentUser: CurrentUser = RequestContextHolder.getContext.getCurrentUser
    if (node.loggedIn && currentUser.getUserId.isEmpty) {
      LoggingHelper.getLogger(getClass).info("This node requires user to be logged in")
      return None
    }
    LoggingHelper.getLogger(getClass).info("Checking node access rights")
    val accessType: NodeAccessType = node.checkRights(currentUser)
    if (accessType eq NodeAccessType.NO_ACCESS) {
      LoggingHelper.getLogger(getClass).info("Current user has no access to specified node")
      return None
    }
    if (filter != null && node.parent.isDefined) {
      traverseParents(node.parent.get, filter)
    }
    if (node.handlerType == null) {
      LoggingHelper.getLogger(getClass).log(Level.WARNING, "Node handler is not set for node path ''{0}''", path)
      return None
    }

    if (node.handlerType.isEmpty) {
      return None
    }

    val handlerType =
      if ((node.handlerType.isDefined && node.handlerType.get == classOf[DefaultRootNodeHandlerFactory]) && StringHelper.isNullOrEmpty(RequestContextHolder.getContext.getEngineConfiguration.getConnectionString)) {
        classOf[DatabaseConfigurationNodeHandlerFactory]
      } else {
        node.handlerType.get
      }

    val nodeHandlerFactory = DependencyResolver(handlerType)
    if (nodeHandlerFactory == null) {
      return None
    }

    val configuration = new NodeHandlerConfiguration(
      objectId = Option(node.nodeId),
      settings = if (node.settings != null) Option(JsonSerializer.deserializeToTree(node.settings).getAsJsonObject) else None,
      accessType = accessType,
      path = node.path,
      fullPath = node.fullPath
    )

    val nodeHandler = nodeHandlerFactory.createNodeHandler(configuration)

    val currentTitle: String = node.title
    val currentPath: String = node.fullPath

    def handleRest(rest: Seq[String], nodeHandler: NodeHandler, first: Boolean, title: String, path: String): Option[(NodeHandler, Seq[String])] = {
      if (filter.isDefined && filter.get(nodeHandler, currentTitle, currentPath, rest, if (first) Option(node.nodeId) else None)) {
        return None
      }

      val childSettings = nodeHandler.createChildHandler(rest)
      if (childSettings.isEmpty) {
        return None
      }

      val childNodeHandler = childSettings.get.handler
      val childTitle = childSettings.get.title
      val childPath = path + "/" + childSettings.get.path
      if (childSettings.get.rest.isEmpty) {
        return Option((childNodeHandler, rest))
      }

      handleRest(childSettings.get.rest, childNodeHandler, first = false, childTitle, childPath)
    }

    val initialRest = foundNode.get._2.split("/")

    val newNodeHandler = handleRest(initialRest, nodeHandler, first = true, currentTitle, currentPath)

    if (newNodeHandler.isEmpty) {
      return None
    }

    if (filter.isDefined && filter.get(newNodeHandler.get._1, currentTitle, currentPath, newNodeHandler.get._2,
        if (initialRest.nonEmpty) Option(node.nodeId) else None)) {
      return None
    }

    Option(newNodeHandler.get._1)
  }
}