/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.globalconfiguration

import com.google.gson.JsonObject
import com.google.inject.Inject
import com.lessmarkup.TextIds
import com.lessmarkup.dataobjects.Module
import com.lessmarkup.framework.helpers.{DependencyResolver, JsonSerializer}
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.annotations.{ConfigurationHandler, RecordAction}
import com.lessmarkup.interfaces.cache.{DataCache, EntityChangeType}
import com.lessmarkup.interfaces.data.{ChangeTracker, DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}
import com.lessmarkup.userinterface.model.global.ModuleModel
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler

class ModulesNodeHandlerFactory @Inject() (domainModelProvider: DomainModelProvider, dataCache: DataCache, changeTracker: ChangeTracker) extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new ModulesNodeHandler(domainModelProvider, dataCache, changeTracker, nodeHandlerConfiguration)
  }
}

@ConfigurationHandler(titleTextId = TextIds.MODULES)
class ModulesNodeHandler(domainModelProvider: DomainModelProvider, dataCache: DataCache, changeTracker: ChangeTracker, configuration: NodeHandlerConfiguration)
  extends RecordListNodeHandler[ModuleModel](domainModelProvider, dataCache, classOf[ModuleModel], configuration) {

  @RecordAction(nameTextId = TextIds.ENABLE, visible = "!enabled")
  def enableModule(recordId: Long, filter: String): JsonObject = {
    enableModule(recordId, enable = true)
  }

  def disableModule(recordId: Long, filter: String): JsonObject = {
    enableModule(recordId, enable = false)
  }

  protected def enableModule(moduleId: Long, enable: Boolean): JsonObject = {
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val siteModule: Module = domainModel.query.find(classOf[Module], moduleId).get
      siteModule.enabled = enable
      domainModel.update(siteModule)
      changeTracker.addChange(classOf[Module], siteModule, EntityChangeType.UPDATED, domainModel)
      val collection: ModuleModel.ModuleModelCollection = DependencyResolver.resolve(classOf[ModuleModel.ModuleModelCollection])
      val record: ModuleModel = collection.read(domainModel.query, Seq(moduleId)).head
      val ret: JsonObject = new JsonObject
      ret.addProperty("index", getIndex(record, null, domainModel))
      ret.add("record", JsonSerializer.serializePojoToTree(record))
      ret
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }
}