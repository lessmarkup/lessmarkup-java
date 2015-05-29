/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

class NavigationBarModel(url: String, title: String, imageUrl: String, selected: Boolean, level: Int) extends MenuItemModel(url, title, imageUrl, selected, level) {

  private final val children: List[NavigationBarModel] = List()

  def getChildren: List[NavigationBarModel] = children
}
