/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.language

class XmlTranslation {
  private var id: String = null
  private var text: String = null

  def getId: String = id

  def setId(id: String) {
    this.id = id
  }

  def getText: String = text

  def setText(text: String) {
    this.text = text
  }
}