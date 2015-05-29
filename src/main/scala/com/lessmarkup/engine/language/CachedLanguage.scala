/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.language

import com.lessmarkup.interfaces.annotations.Implements
import com.lessmarkup.interfaces.system.Language

@Implements(classOf[Language])
class CachedLanguage(
  val name: String,
  val shortName: String,
  val iconId: Option[Long] = None,
  val isDefault: Boolean = false,
  val translations: Map[String, String],
  val id: Option[Long] = None
  ) extends Language {

  def getIconId = iconId
  def getShortName = shortName
  def getIsDefault = isDefault
  def getTranslations = translations
  def getName = name
  def getText(id: String): Option[String] = getText(id, throwIfNotFound = true)

  def getText(id: String, throwIfNotFound: Boolean): Option[String] = {
    val cachedTranslation: Option[String] = translations.get(id)
    if (cachedTranslation.isEmpty && throwIfNotFound) {
      throw new IllegalArgumentException
    }
    cachedTranslation
  }
}
