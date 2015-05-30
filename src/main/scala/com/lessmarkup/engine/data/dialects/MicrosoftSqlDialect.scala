/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data.dialects

class MicrosoftSqlDialect extends DatabaseLanguageDialect {

  def getTypeDeclaration(dataType: DatabaseDataType, sizeLimit: Option[Int], required: Boolean, characterSet: Option[String]): String = {
    val nullable: String = if (required) " NOT NULL" else " NULL"
    dataType match {
      case DatabaseDataType.INT => "[INT]" + nullable
      case DatabaseDataType.LONG => "[BIGINT]" + nullable
      case DatabaseDataType.DATE_TIME => "[DATETIME]" + nullable
      case DatabaseDataType.IDENTITY => "[BIGINT] IDENTITY(1,1)"
      case DatabaseDataType.BOOLEAN => "[BIT]" + nullable
      case DatabaseDataType.FLOAT => "[FLOAT]" + nullable
      case DatabaseDataType.DOUBLE => "[DOUBLE]" + nullable
      case DatabaseDataType.STRING => String.format("[NVARCHAR](%s)%s", if (sizeLimit.isDefined) Integer.toString(sizeLimit.get) else "max", nullable)
      case DatabaseDataType.BINARY => String.format("[VARBINARY](%s)%s", if (sizeLimit.isDefined) Integer.toString(sizeLimit.get) else "max", nullable)
      case _ => throw new IllegalArgumentException
    }
  }

  def getDataType(dataType: String): DatabaseDataType = {
    dataType.toUpperCase match {
      case "INT" => DatabaseDataType.INT
      case "BIGINT" => DatabaseDataType.LONG
      case "DATETIME" => DatabaseDataType.DATE_TIME
      case "BIT" => DatabaseDataType.BOOLEAN
      case "FLOAT" => DatabaseDataType.FLOAT
      case "DOUBLE" => DatabaseDataType.DOUBLE
      case "NVARCHAR" => DatabaseDataType.STRING
      case "VARBINARY" => DatabaseDataType.BINARY
      case _ => null
    }
  }

  def paging(from: Int, count: Int): String = {
    s"OFFSET $from ROWS FETCH NEXT $count ROWS ONLY"
  }

  def decorateName(name: String): String = {
    "[" + name + "]"
  }

  override def schemaTablesName: String = "INFORMATION_SCHEMA.TABLES"

  override def definePrimaryKey: Boolean = true
}
