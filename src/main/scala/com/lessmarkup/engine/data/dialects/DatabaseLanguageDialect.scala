/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data.dialects

trait DatabaseLanguageDialect {
  def getTypeDeclaration(dataType: DatabaseDataType, sizeLimit: Option[Int], required: Boolean, characterSet: Option[String]): String

  def getTypeDeclaration(dataType: DatabaseDataType, required: Boolean): String = {
    getTypeDeclaration(dataType, None, required, None)
  }

  def decorateName(name: String): String

  def getDataType(dataType: String): DatabaseDataType

  def paging(from: Int, count: Int): String

  def schemaTablesName: String

  def definePrimaryKey: Boolean

  def constructAddForeignKeyStatement(dependentTableName: String, constraintName: String, column: String, baseTableName: String, idProperty: String) =
    s"ALTER TABLE $dependentTableName ADD CONSTRAINT $constraintName FOREIGN KEY($column) REFERENCES $baseTableName ($idProperty)"
}
