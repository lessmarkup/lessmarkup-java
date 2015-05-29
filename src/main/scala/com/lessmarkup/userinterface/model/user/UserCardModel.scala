/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.user

import com.google.inject.Inject
import com.lessmarkup.TextIds
import com.lessmarkup.dataobjects.User
import com.lessmarkup.interfaces.annotations.RecordColumn
import com.lessmarkup.interfaces.data.{DomainModelProvider, QueryBuilder}
import com.lessmarkup.interfaces.recordmodel.{ModelCollection, RecordModel}

class UserCardModelCollection @Inject()(domainModelProvider: DomainModelProvider) extends ModelCollection[UserCardModel] {

  def readIds(query: QueryBuilder, ignoreOrder: Boolean): Seq[Long] = {
    query.from(classOf[User]).where("removed = $", false).toIdList
  }

  def getCollectionId: Int = {
    domainModelProvider.getCollectionId(classOf[User]).get
  }

  def read(queryBuilder: QueryBuilder, ids: Seq[Long]): Seq[UserCardModel] = {
    for (user <- queryBuilder.from(classOf[User]).where("removed = $", false).toList(classOf[User])) yield {
      val model: UserCardModel = new UserCardModel
      model.id = user.id
      model.name = user.name
      model.title = user.title
      model.signature = user.signature
      model
    }
  }
}

class UserCardModel extends RecordModel[UserCardModel](classOf[UserCardModelCollection], classOf[User]) {

  @RecordColumn(textId = TextIds.NAME)
  var name: String = null
  @RecordColumn(textId = TextIds.TITLE)
  var title: Option[String] = None
  @RecordColumn(textId = TextIds.SIGNATURE)
  var signature: Option[String] = None
}