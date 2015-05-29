/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

object OptionInt {
  def apply(v: Int) = new OptionInt(v)
  def apply() = new OptionInt()
  def apply(v: Option[Int]) = new OptionInt(v)

  implicit def convert(v: Option[Int]): OptionInt = OptionInt.apply(v)
  implicit def convert(v: Int): OptionInt = OptionInt.apply(v)
  implicit def convert(v: OptionInt): Option[Int] = v.option
}

class OptionInt(val option: Option[Int]) {

  def isEmpty = option.isEmpty
  def isDefined = option.isDefined
  def get = option.get

  def this() = {
    this(Option.empty)
  }

  def this(v: Int) = {
    this(Option(v))
  }
}
