package com.lessmarkup.interfaces.data

class OptionInt(option: Option[Int]) {
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
