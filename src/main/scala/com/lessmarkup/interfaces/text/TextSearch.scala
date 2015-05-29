/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.text

import com.lessmarkup.interfaces.cache.CacheHandler
import com.lessmarkup.interfaces.data.DomainModel

trait TextSearch extends CacheHandler {
  def search(text: String, startRecord: Int, recordCount: Int, domainModel: DomainModel): SearchResults
}