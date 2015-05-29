/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.system

import com.lessmarkup.interfaces.cache.CacheHandler

trait LanguageCache extends CacheHandler {
  def getCurrentLanguageId: Option[String]

  def getTranslation(id: String, moduleType: Option[String], throwIfNotFound: Boolean): Option[String] = {
    getTranslation(getCurrentLanguageId, id, moduleType, throwIfNotFound)
  }

  def getTranslation(id: String, moduleType: Option[String]): Option[String] = {
    getTranslation(id, moduleType, throwIfNotFound = true)
  }

  def getTranslation(languageId: Option[String], id: String, moduleType: Option[String], throwIfNotFound: Boolean): Option[String]

  def getTranslation(languageId: Option[String], id: String, moduleType: Option[String]): Option[String] = {
    getTranslation(languageId, id, moduleType, throwIfNotFound = true)
  }

  def getLanguages: List[Language]
}