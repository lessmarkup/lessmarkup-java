/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data.dialects

import com.lessmarkup.engine.data.dialects.DatabaseDataType._

class MicrosoftSqlDialect extends DatabaseLanguageDialect {

  def getTypeDeclaration(dataType: DatabaseDataType, sizeLimit: Option[Int], required: Boolean, characterSet: Option[String]): String = {
    val nullable: String = if (required) " NOT NULL" else " NULL"
    dataType match {
      case INT => "[INT]" + nullable
      case LONG => "[BIGINT]" + nullable
      case DATE_TIME => "[DATETIME]" + nullable
      case IDENTITY => "[BIGINT] IDENTITY(1,1)" + nullable
      case BOOLEAN => "[BIT]" + nullable
      case FLOAT => "[FLOAT]" + nullable
      case DOUBLE => "[DOUBLE]" + nullable
      case STRING => String.format("[NVARCHAR](%s)%s", if (sizeLimit.isDefined) Integer.toString(sizeLimit.get) else "max", nullable)
      case BINARY => String.format("[VARBINARY](%s)%s", if (sizeLimit.isDefined) Integer.toString(sizeLimit.get) else "max", nullable)
      case _ => throw new IllegalArgumentException
    }
  }

  def getDataType(dataType: String): DatabaseDataType = {
    dataType.toUpperCase match {
      case "INT" => INT
      case "BIGINT" => LONG
      case "DATETIME" => DATE_TIME
      case "BIT" => BOOLEAN
      case "FLOAT" => FLOAT
      case "DOUBLE" => DOUBLE
      case "NVARCHAR" => STRING
      case "VARBINARY" => BINARY
      case _ => null
    }
  }

  def paging(from: Int, count: Int): String = {
    s"OFFSET $from ROWS FETCH NEXT $count ROWS ONLY"
  }

  def decorateName(name: String): String = {
    "[" + name + "]"
  }
}
