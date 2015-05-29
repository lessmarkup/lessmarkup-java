/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.common

import java.util.logging.{Level, Logger}

import com.google.gson.{JsonArray, JsonElement, JsonObject, JsonPrimitive}
import com.google.inject.Inject
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.text.{SearchResults, TextSearch}

class SearchTextModel @Inject() (dataCache: DataCache, domainModelProvider: DomainModelProvider) {

  def handle(searchText: String): Option[JsonElement] = {
    val searchCache: TextSearch = dataCache.get(classOf[TextSearch])
    val domainModel: DomainModel = this.domainModelProvider.create
    try {
      val results: SearchResults = searchCache.search(searchText, 0, 10, domainModel)
      if (results == null) {
        return None
      }
      val array: JsonArray = new JsonArray
      results.getResults.foreach(r => {
        val obj = new JsonObject()
        obj.add("name", new JsonPrimitive(r.getName))
        obj.add("text", new JsonPrimitive(r.getText))
        obj.add("url", new JsonPrimitive(r.getUrl))
        array.add(obj)
      })
      Option(array)
    }
    catch {
      case ex: Exception =>
        Logger.getLogger(classOf[SearchTextModel].getName).log(Level.SEVERE, null, ex)
        None
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }
}