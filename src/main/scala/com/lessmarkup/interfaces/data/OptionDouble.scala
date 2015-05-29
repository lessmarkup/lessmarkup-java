/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

object OptionDouble {
  def apply(v: Double) = new OptionDouble(v)
  def apply() = new OptionDouble()
  def apply(v: Option[Double]) = new OptionDouble(v)

  implicit def convert(v: Option[Double]): OptionDouble = OptionDouble.apply(v)
  implicit def convert(v: Double): OptionDouble = OptionDouble.apply(v)
  implicit def convert(v: OptionDouble): Option[Double] = v.option
}

class OptionDouble(val option: Option[Double]) {

  def isEmpty = option.isEmpty
  def isDefined = option.isDefined
  def get = option.get

  def this() = {
    this(Option.empty)
  }

  def this(v: Double) = {
    this(Option(v))
  }
}
