/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

import com.google.gson.JsonObject
import com.google.inject.Inject
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.DomainModelProvider
import com.lessmarkup.interfaces.structure.NodeCache

class TypeaheadModel @Inject() (dataCache: DataCache, domainModelProvider: DomainModelProvider) {
  //private var records: List[String] = null

  def initialize(path: String, property: String, searchText: String) {
    val nodeCache: NodeCache = this.dataCache.get(classOf[NodeCache])
    val node = nodeCache.getNode(path)
    if (node.isEmpty) {
      throw new IllegalArgumentException("Cannot find node")
    }
    /*var handler: NodeHandler = DependencyResolver.resolve(node._1.getHandlerType)
    val collectionManagers: List[PropertyCollectionManager] = new ArrayList[PropertyCollectionManager]
    if (classOf[PropertyCollectionManager].isAssignableFrom(node._1.getHandlerType)) {
      collectionManagers.add(handler.asInstanceOf[PropertyCollectionManager])
    }
    var rest: String = node._2
    while (rest != null && rest.length > 0) {
      val childSettings: ChildHandlerSettings = handler.getChildHandler(rest)
      if (childSettings == null || !childSettings.getId.isPresent) {
        throw new IllegalArgumentException
      }
      handler = childSettings.getHandler
      if (classOf[PropertyCollectionManager].isAssignableFrom(handler.getClass)) {
        collectionManagers.add(handler.asInstanceOf[PropertyCollectionManager])
      }
      rest = childSettings.getRest
    }
    if (!collectionManagers.isEmpty) {
      try {
        val domainModel: DomainModel = this.domainModelProvider.create
        try {
          import scala.collection.JavaConversions._
          for (manager <- collectionManagers) {
            val collection: List[String] = manager.getCollection(domainModel, property, searchText)
            if (collection == null || collection.isEmpty) {
              continue //todo: continue is not supported
            }
            records = new ArrayList[String]
            collection.stream.limit(10).forEach(records.add)
            return
          }
        }
        catch {
          case ex: Exception => {
            Logger.getLogger(classOf[TypeaheadModel].getName).log(Level.SEVERE, null, ex)
          }
        } finally {
          if (domainModel != null) domainModel.close()
        }
      }
    }*/
  }

  def toJson: JsonObject = {
    val builder: JsonObject = new JsonObject
    /*val array: JsonArray = new JsonArray
    if (this.records != null) {
      import scala.collection.JavaConversions._
      for (record <- records) {
        array.add(new JsonPrimitive(record))
      }
    }
    builder.add("records", array)*/
    builder
  }
}