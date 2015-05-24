/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.scripting

object AtomType {
  private def apply(value: Int) = new AtomType(value)

  final val OBJECT = AtomType(1)
  final val EQUAL = AtomType(2)
  final val NOT_EQUAL = AtomType(3)
  final val INVERSE = AtomType(4)
  final val OPEN = AtomType(5)
  final val CLOSE = AtomType(6)
  final val PARAMETER = AtomType(7)
  final val AND = AtomType(8)
  final val OR = AtomType(9)
  final val PLUS = AtomType(10)
  final val MINUS = AtomType(11)
  final val NULL = AtomType(12)
}

class AtomType(value: Int)