package com.lessmarkup.interfaces.data

class OptionLong(option: Option[Long]) {

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
