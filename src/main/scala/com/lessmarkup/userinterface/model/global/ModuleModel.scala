/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.global

import com.google.inject.Inject
import com.lessmarkup.TextIds
import com.lessmarkup.dataobjects.Module
import com.lessmarkup.interfaces.annotations.RecordColumn
import com.lessmarkup.interfaces.data.{DomainModelProvider, QueryBuilder}
import com.lessmarkup.interfaces.recordmodel.{ModelCollection, RecordModel}
import com.lessmarkup.userinterface.model.global.ModuleModel.ModuleModelCollection

object ModuleModel {

  class ModuleModelCollection @Inject() (domainModelProvider: DomainModelProvider) extends ModelCollection[ModuleModel] {

    def readIds(query: QueryBuilder, ignoreOrder: Boolean): Seq[Long] = {
      val changedQuery = query.from(classOf[Module]).where("removed = $ AND system = $", false, false)
      changedQuery.toIdList
    }

    def getCollectionId: Int = {
      domainModelProvider.getCollectionId(classOf[Module]).get
    }

    def read(queryBuilder: QueryBuilder, ids: Seq[Long]): Seq[ModuleModel] = {
      queryBuilder.from(classOf[Module]).where("removed = $ AND system = $", false, false).whereIds(ids).toList(classOf[Module]).map(module => {
        val model: ModuleModel = new ModuleModel
        model.enabled = module.enabled
        model.name = module.name
        model.moduleId = module.id
        model
      })
    }
  }
}

class ModuleModel extends RecordModel[ModuleModel](classOf[ModuleModelCollection], classOf[Module]) {
  var moduleId: Long = 0L
  @RecordColumn(textId = TextIds.NAME)
  var name: String = null
  @RecordColumn(textId = TextIds.ENABLED)
  var enabled: Boolean = false
}
