/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

object OptionString {
  def apply(v: String) = new OptionString(v)
  def apply() = new OptionString()
  def apply(v: Option[String]) = new OptionString(v)

  implicit def convert(v: Option[String]): OptionString = OptionString.apply(v)
  implicit def convert(v: String): OptionString = OptionString.apply(v)
  implicit def convert(v: OptionString): Option[String] = v.option
}

class OptionString(val option: Option[String]) {
  def isEmpty = option.isEmpty
  def isDefined = option.isDefined
  def get = option.get

  def this() = {
    this(Option.empty)
  }

  def this(v: String) = {
    this(Option(v))
  }
}
