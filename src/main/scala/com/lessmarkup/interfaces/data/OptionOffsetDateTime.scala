/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

import java.time.OffsetDateTime

object OptionOffsetDateTime {
  def apply(v: OffsetDateTime) = new OptionOffsetDateTime(v)
  def apply() = new OptionOffsetDateTime()
  def apply(v: Option[OffsetDateTime]) = new OptionOffsetDateTime(v)

  implicit def convert(v: Option[OffsetDateTime]): OptionOffsetDateTime = OptionOffsetDateTime.apply(v)
  implicit def convert(v: OffsetDateTime): OptionOffsetDateTime = OptionOffsetDateTime.apply(v)
  implicit def convert(v: OptionOffsetDateTime): Option[OffsetDateTime] = v.option
}

class OptionOffsetDateTime(val option: Option[OffsetDateTime]) {

  def isEmpty = option.isEmpty
  def isDefined = option.isDefined
  def get = option.get

  def this() = {
    this(Option.empty)
  }

  def this(v: OffsetDateTime) = {
    this(Option(v))
  }
}
