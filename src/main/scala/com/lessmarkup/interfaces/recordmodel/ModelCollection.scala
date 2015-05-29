/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.recordmodel

import com.lessmarkup.interfaces.data.QueryBuilder

trait ModelCollection[T] {
  def readIds(query: QueryBuilder, ignoreOrder: Boolean): Seq[Long]
  def getCollectionId: Int
  def read(queryBuilder: QueryBuilder, ids: Seq[Long]): Seq[T]
}
