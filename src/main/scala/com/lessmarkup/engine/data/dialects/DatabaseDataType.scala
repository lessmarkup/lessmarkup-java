package com.lessmarkup.engine.data.dialects

object DatabaseDataType {
  private def apply(value: Int) = new DatabaseDataType(value)
  final val INT = DatabaseDataType(1)
  final val LONG = DatabaseDataType(1)
  final val STRING = DatabaseDataType(2)
  final val DATE_TIME = DatabaseDataType(3)
  final val IDENTITY = DatabaseDataType(4)
  final val BOOLEAN = DatabaseDataType(5)
  final val FLOAT = DatabaseDataType(6)
  final val DOUBLE = DatabaseDataType(7)
  final val BINARY = DatabaseDataType(8)
}

class DatabaseDataType(val value: Int)
