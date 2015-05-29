/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.recordmodel

import com.lessmarkup.interfaces.data.DomainModel

trait PropertyCollectionManager {
  def getCollection(domainModel: DomainModel, property: String, searchText: String): Seq[String]
}
