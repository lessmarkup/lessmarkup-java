/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

object OptionBool {
  def apply(v: Boolean) = new OptionBool(v)
  def apply() = new OptionBool()
  def apply(v: Option[Boolean]) = new OptionBool(v)

  implicit def convert(v: Option[Boolean]): OptionBool = OptionBool.apply(v)
  implicit def convert(v: Boolean): OptionBool = OptionBool.apply(v)
  implicit def convert(v: OptionBool): Option[Boolean] = v.option
}

class OptionBool(val option: Option[Boolean]) {

  def isEmpty = option.isEmpty
  def isDefined = option.isDefined
  def get = option.get

  def this() = {
    this(Option.empty)
  }

  def this(v: Boolean) = {
    this(Option(v))
  }
}
