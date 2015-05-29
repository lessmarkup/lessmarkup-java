/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

class MenuItemModel(url: String, title: String, imageUrl: String, selected: Boolean, level: Int) {

  def getUrl: String = url

  def getTitle: String = title

  def getImageUrl: String = imageUrl

  def isSelected: Boolean = selected

  def getLevel: Int = level
}
