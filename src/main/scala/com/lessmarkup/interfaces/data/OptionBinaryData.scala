/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

object OptionBinaryData {
  def apply(v: BinaryData) = new OptionBinaryData(v)
  def apply() = new OptionBinaryData()
  def apply(v: Option[BinaryData]) = new OptionBinaryData(v)

  implicit def convert(v: Option[BinaryData]): OptionBinaryData = OptionBinaryData.apply(v)
  implicit def convert(v: BinaryData): OptionBinaryData = OptionBinaryData.apply(v)
  implicit def convert(v: OptionBinaryData): Option[BinaryData] = v.option
}

class OptionBinaryData(val option: Option[BinaryData]) {

  def isEmpty = option.isEmpty
  def isDefined = option.isDefined
  def get = option.get

  def this() = {
    this(None)
  }

  def this(v: BinaryData) = {
    this(Option(v))
  }
}
