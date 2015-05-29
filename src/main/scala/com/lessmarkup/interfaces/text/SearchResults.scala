/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.text

import scala.collection.mutable

class SearchResults {
  private final val results: mutable.ListBuffer[SearchResult] = mutable.ListBuffer()
  private var actualCount: Int = 0

  def getResults: List[SearchResult] = {
    this.results.toList
  }

  def getActualCount: Int = {
    this.actualCount
  }

  def setActualCount(`val`: Int) {
    this.actualCount = `val`
  }
}