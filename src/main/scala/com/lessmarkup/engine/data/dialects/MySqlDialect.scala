/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data.dialects

class MySqlDialect extends DatabaseLanguageDialect {
  def getTypeDeclaration(dataType: DatabaseDataType, sizeLimit: Option[Int], required: Boolean, characterSet: Option[String]): String = {
    val nullable: String = if (required) " NOT NULL" else " NULL"
    dataType match {
      case DatabaseDataType.INT => "INT" + nullable
      case DatabaseDataType.LONG => "BIGINT" + nullable
      case DatabaseDataType.DATE_TIME => "DATETIME" + nullable
      case DatabaseDataType.STRING =>
        if (sizeLimit.isEmpty)
          "TEXT CHARACTER SET " + characterSet.getOrElse("utf8") + nullable
        else
          s"VARCHAR(${sizeLimit.get}) CHARACTER SET ${characterSet.getOrElse("utf8")} $nullable"
      case DatabaseDataType.BOOLEAN => "BIT" + nullable
      case DatabaseDataType.FLOAT => "REAL" + nullable
      case DatabaseDataType.DOUBLE => "DOUBLE" + nullable
      case DatabaseDataType.BINARY => "BLOB" + nullable
      case DatabaseDataType.IDENTITY => "BIGINT AUTO_INCREMENT"
      case _ => throw new IllegalArgumentException
    }
  }

  def decorateName(name: String): String = {
    "`" + name + "`"
  }

  def getDataType(dataType: String): DatabaseDataType = {
    dataType.toUpperCase match {
      case "INT" => DatabaseDataType.INT
      case "BIGINT" => DatabaseDataType.LONG
      case "DATETIME" => DatabaseDataType.DATE_TIME
      case "TEXT" => DatabaseDataType.STRING
      case "VARCHAR" => DatabaseDataType.STRING
      case "BIT" => DatabaseDataType.BOOLEAN
      case "REAL" => DatabaseDataType.FLOAT
      case "DOUBLE" => DatabaseDataType.DOUBLE
      case "BLOB" => DatabaseDataType.BINARY
      case _ => null
    }
  }

  def paging(from: Int, count: Int): String = {
    s"LIMIT $from,$count"
  }

  override def schemaTablesName: String = "INFORMATION_SCHEMA.TABLES"

  override def definePrimaryKey: Boolean = true
}
