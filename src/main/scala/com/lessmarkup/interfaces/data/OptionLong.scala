/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

object OptionLong {
  def apply(v: Long) = new OptionLong(v)
  def apply() = new OptionLong()
  def apply(v: Option[Long]) = new OptionLong(v)

  implicit def convert(v: Option[Long]): OptionLong = OptionLong.apply(v)
  implicit def convert(v: Long): OptionLong = OptionLong.apply(v)
  implicit def convert(v: OptionLong): Option[Long] = v.option
}

class OptionLong(val option: Option[Long]) {

  def isEmpty = option.isEmpty
  def isDefined = option.isDefined
  def get = option.get

  def this() = {
    this(Option.empty)
  }

  def this(v: Long) = {
    this(Option(v))
  }
}
