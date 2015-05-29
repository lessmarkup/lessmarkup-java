/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

import com.lessmarkup.interfaces.cache.CacheHandler

trait ChangesCache extends CacheHandler {
  def getLastChangeId: Option[Long]
  def getCollectionChanges(collectionId: Int, fromId: Option[Long], toId: Option[Long], filterFunc: Option[DataChange => Boolean] = None): Seq[DataChange]
}
