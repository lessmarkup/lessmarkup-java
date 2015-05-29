/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.configuration

import com.google.inject.Inject
import com.lessmarkup.dataobjects.{User, UserGroup}
import com.lessmarkup.framework.helpers.{DependencyResolver, StringHelper}
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.recordmodel.{ModelCollection, PropertyCollectionManager}
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}
import com.lessmarkup.userinterface.model.configuration.{NodeAccessModel, NodeAccessModelCollectionManager}
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler

class NodeAccessNodeHandlerFactory(domainModelProvider: DomainModelProvider,
                                   dataCache: DataCache,
                                   configuration: NodeHandlerConfiguration)
  extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    if (arguments.length != 1) {
      throw new IllegalArgumentException
    }
    new NodeAccessNodeHandler(domainModelProvider, dataCache, configuration, arguments.head.asInstanceOf)
  }
}

class NodeAccessNodeHandler @Inject()(
  domainModelProvider: DomainModelProvider,
  dataCache: DataCache,
  configuration: NodeHandlerConfiguration,
  nodeId: Long)
  extends RecordListNodeHandler[NodeAccessModel](domainModelProvider, dataCache, classOf[NodeAccessModel], configuration)
  with PropertyCollectionManager {

  protected override def createCollection: ModelCollection[NodeAccessModel] = {
    DependencyResolver.resolve(classOf[NodeAccessModelCollectionManager], nodeId)
  }

  def getCollection(domainModel: DomainModel, property: String, searchText: String): Seq[String] = {
    if (StringHelper.isNullOrWhitespace(searchText)) {
      throw new IllegalArgumentException("searchText")
    }
    val searchText2: String = "%" + searchText + "%"
    property match {
      case "user" =>
        domainModel.query
          .from(classOf[User])
          .where("name LIKE $ OR email LIKE $", searchText, searchText)
          .toList(classOf[User], Option("email")).map(_.email)
      case "group" =>
        domainModel.query
          .from(classOf[UserGroup])
          .where("name LIKE $", searchText2)
          .toList(classOf[UserGroup], Option("name")).map(_.name)
      case _ =>
        Nil
    }
  }
}
