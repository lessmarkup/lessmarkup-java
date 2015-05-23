package com.lessmarkup.interfaces.data

class OptionDouble(option: Option[Double]) {

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
