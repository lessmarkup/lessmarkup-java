/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.system

trait Language {
  def getName: String
  def getIconId: Option[Long]
  def getShortName: String
  def getIsDefault: Boolean
  def getTranslations: Map[String, String]
}
