/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.language

class XmlLanguageFile {
  private var name: String = null
  private var id: String = null
  private var shortName: String = null
  private final val translations: java.util.List[XmlTranslation] = new java.util.ArrayList[XmlTranslation]

  def getName: String = name

  def setName(name: String) {
    this.name = name
  }

  def getId: String = id

  def setId(id: String) {
    this.id = id
  }

  def getShortName: String = shortName

  def setShortName(shortName: String) {
    this.shortName = shortName
  }

  def getTranslations: java.util.List[XmlTranslation] = {
    translations
  }
}