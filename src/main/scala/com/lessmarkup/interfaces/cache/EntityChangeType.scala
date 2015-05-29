/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.cache

object EntityChangeType {

  def apply(value: Int) = new EntityChangeType(value)

  final val ADDED = EntityChangeType(1)
  final val REMOVED = EntityChangeType(2)
  final val UPDATED = EntityChangeType(3)
}

class EntityChangeType(val value: Int)
