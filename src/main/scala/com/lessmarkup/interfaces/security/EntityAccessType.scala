/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.security

object EntityAccessType {
  private def apply(value: Int) = new EntityAccessType(value)
  val EVERYONE = EntityAccessType(0)
  val READ = EntityAccessType(1)
  val READ_WRITE = EntityAccessType(2)
}

class EntityAccessType(val value: Int)