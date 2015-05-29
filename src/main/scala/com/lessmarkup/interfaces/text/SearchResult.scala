/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.text

class SearchResult {
  private var name: String = null
  private var text: String = null
  private var url: String = null

  def getName: String = this.name

  def setName(value: String) {
    this.name = value
  }

  def getText: String = this.text

  def setText(value: String) {
    this.text = value
  }

  def getUrl: String = this.url

  def setUrl(value: String) {
    this.url = value
  }
}