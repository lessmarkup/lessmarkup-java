/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data.dialects

class HsqldbDialect extends DatabaseLanguageDialect {
  override def getTypeDeclaration(dataType: DatabaseDataType, sizeLimit: Option[Int], required: Boolean, characterSet: Option[String]): String = {
    val nullable: String = if (required) " NOT NULL" else " NULL"
    dataType match {
      case DatabaseDataType.INT => "INTEGER" + nullable
      case DatabaseDataType.LONG => "BIGINT" + nullable
      case DatabaseDataType.DATE_TIME => "TIMESTAMP" + nullable
      case DatabaseDataType.STRING =>
        if (sizeLimit.isEmpty)
          "VARCHAR(2048) " + nullable
        else
          s"VARCHAR(${sizeLimit.get}) $nullable"
      case DatabaseDataType.BOOLEAN => "BOOLEAN" + nullable
      case DatabaseDataType.FLOAT => "NUMERIC" + nullable
      case DatabaseDataType.DOUBLE => "DOUBLE" + nullable
      case DatabaseDataType.BINARY => "BLOB" + nullable
      case DatabaseDataType.IDENTITY => "BIGINT IDENTITY"
      case _ => throw new IllegalArgumentException
    }
  }

  override def paging(from: Int, count: Int): String = {
    s"LIMIT $count OFFSET $from"
  }

  override def getDataType(dataType: String): DatabaseDataType = {
    dataType.toUpperCase match {
      case "INTEGER" => DatabaseDataType.INT
      case "BIGINT" => DatabaseDataType.LONG
      case "TIMESTAMP" => DatabaseDataType.DATE_TIME
      case "VARCHAR" => DatabaseDataType.STRING
      case "BOOLEAN" => DatabaseDataType.BOOLEAN
      case "NUMERIC" => DatabaseDataType.FLOAT
      case "DOUBLE" => DatabaseDataType.DOUBLE
      case "BLOB" => DatabaseDataType.BINARY
      case _ => null
    }
  }

  override def decorateName(name: String): String = "\"" + name + "\""

  override def schemaTablesName: String = "INFORMATION_SCHEMA.SYSTEM_TABLES"

  override def definePrimaryKey: Boolean = false

  override def constructAddForeignKeyStatement(dependentTableName: String, constraintName: String, column: String, baseTableName: String, idProperty: String) =
    s"ALTER TABLE $dependentTableName ADD FOREIGN KEY($column) REFERENCES $baseTableName ($idProperty)"
}
