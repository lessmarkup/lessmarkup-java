package com.lessmarkup.interfaces.data

class OptionBool(option: Option[Boolean]) {

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
