/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data.migrate

import java.sql.{Connection, ResultSet, SQLException, Statement}
import java.time.OffsetDateTime
import com.lessmarkup.Constants
import com.lessmarkup.engine.data.ConnectionManager
import com.lessmarkup.engine.data.dialects.{DatabaseDataType, DatabaseLanguageDialect, DatabaseLanguageDialectFactory}
import com.lessmarkup.framework.helpers.{LoggingHelper, PropertyDescriptor, StringHelper, TypeHelper}
import com.lessmarkup.interfaces.annotations.{RequiredField, MaxLength}
import com.lessmarkup.interfaces.data._
import com.lessmarkup.interfaces.exceptions.DatabaseException
import org.atteo.evo.inflector.English

import scala.collection.JavaConversions._

class MigratorImpl(connectionString: String) extends Migrator {

  private val connection: Option[Connection] =
    if (StringHelper.isNullOrEmpty(connectionString))
      None
    else
      Option(ConnectionManager.getConnection(connectionString))

  private val dialect: Option[DatabaseLanguageDialect] =
    if (connection.isDefined)
      Option(DatabaseLanguageDialectFactory.createDialect(connection))
    else
      None

  def executeSql(sql: String) {

    if (connection.isEmpty) {
      return
    }

    val statement: Statement = connection.get.createStatement
    try {
      statement.execute(sql)
    }
    catch {
      case e: SQLException => throw new DatabaseException(e)
    } finally {
      if (statement != null) statement.close()
    }
  }

  def executeScalar(sql: String): AnyRef = {

    if (connection.isEmpty) {
      return null
    }

    val statement: Statement = this.connection.get.createStatement
    val resultSet: ResultSet = statement.executeQuery(sql)
    try {
      if (!resultSet.first) {
        return null
      }
      resultSet.getObject(1)
    }
    catch {
      case e: SQLException => throw new DatabaseException(e)
    } finally {
      if (statement != null) statement.close()
      if (resultSet != null) resultSet.close()
    }
  }

  def checkExists[T <: DataObject](`type`: Class[T]): Boolean = {
    if (connection == null) {
      return true
    }
    val tableName: String = English.plural(`type`.getSimpleName)
    executeScalar(s"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '$tableName'").asInstanceOf[Long] > 0
  }

  private def getDataType(property: PropertyDescriptor): String = {

    if (dialect.isEmpty) {
      throw new IllegalArgumentException
    }

    val propertyType: Class[_] = property.getType
    if (propertyType == classOf[OptionLong]) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.LONG, required = false)
    }
    if (propertyType == classOf[OptionInt]) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.INT, required = false)
    }
    if (propertyType == classOf[OptionBool]) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.BOOLEAN, required = false)
    }
    if (propertyType == classOf[OptionDouble]) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.DOUBLE, required = false)
    }
    val required: Boolean = property.getAnnotation(classOf[RequiredField]) != null
    val maxLengthAnnotation: MaxLength = property.getAnnotation(classOf[MaxLength])
    val maxLength: Option[Int] =
      if (maxLengthAnnotation != null) Option(maxLengthAnnotation.length)
      else None
    if ((propertyType == classOf[Int]) || (propertyType == classOf[Integer])) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.INT, required)
    }
    if (propertyType == classOf[OffsetDateTime]) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.DATE_TIME, required)
    }
    if ((propertyType == classOf[Long]) || (propertyType == classOf[Long])) {
      if (property.getName == Constants.DataIdPropertyName) {
        return dialect.get.getTypeDeclaration(DatabaseDataType.IDENTITY, required)
      }
      return dialect.get.getTypeDeclaration(DatabaseDataType.LONG, required)
    }
    if ((propertyType == classOf[Boolean]) || (propertyType == classOf[Boolean])) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.BOOLEAN, required)
    }
    if ((propertyType == classOf[Double]) || (propertyType == classOf[Double])) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.DOUBLE, required)
    }
    if (propertyType == classOf[String]) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.STRING, maxLength, required, null)
    }
    if (propertyType == classOf[Array[Byte]]) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.BINARY, maxLength, required, null)
    }
    if (propertyType.isEnum) {
      return dialect.get.getTypeDeclaration(DatabaseDataType.INT, required)
    }
    throw new IllegalArgumentException
  }

  def createTable[T <: DataObject](tableType: Class[T]) {

    if (dialect.isEmpty) {
      return
    }

    if (checkExists(tableType)) {
      updateTable(tableType)
      return
    }

    val tableName: String = English.plural(tableType.getSimpleName)
    val sb: StringBuilder = new StringBuilder
    sb.append(s"CREATE TABLE ${dialect.get.decorateName(tableName)} (")

    sb.append(TypeHelper.getProperties(tableType).map(t => {
      val dataType: String = getDataType(t)
      String.format("%s %s", dialect.get.decorateName(t.getName), dataType)
    }).mkString(", "))

    sb.append(String.format(", CONSTRAINT %s PRIMARY KEY (%s ASC))",
      dialect.get.decorateName("PK_" + tableName),
      dialect.get.decorateName(Constants.DataIdPropertyName)))

    try {
      executeSql(sb.toString())
    }
    catch {
      case e: Exception =>
        LoggingHelper.getLogger(getClass).info("Error creating table: " + sb.toString)
        throw e
    }
  }

  private[migrate] def checkDependency[TD <: DataObject, TB <: DataObject](typeD: Class[TD], typeB: Class[TB], column: String): Boolean = {

    if (connection.isEmpty) {
      return true
    }
    val dependentTableName: String = English.plural(typeD.getSimpleName)
    val baseTableName: String = English.plural(typeB.getSimpleName)

    val safeColumn = if (column == null) String.format("%sId", typeB.getSimpleName) else column

    val text: String = String.format("SELECT COUNT(*) FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_NAME = 'FK_%s_%s_%s'",
      dependentTableName, baseTableName, safeColumn)

    executeScalar(text).asInstanceOf[Long] > 0
  }

  def addDependency[TD <: DataObject, TB <: DataObject](typeD: Class[TD], typeB: Class[TB], column: String) {

    if (connection.isEmpty) {
      return
    }

    if (checkDependency(typeD, typeB, column)) {
      return
    }

    val dependentTableName: String = English.plural(typeD.getSimpleName)
    val baseTableName: String = English.plural(typeB.getSimpleName)
    val safeColumn = if (column == null) String.format("%sId", typeB.getSimpleName) else column

    val command1: String = String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY(%s) REFERENCES %s (%s)",
      dialect.get.decorateName(dependentTableName),
      dialect.get.decorateName(String.format("FK_%s_%s_%s", dependentTableName, baseTableName, safeColumn)),
      dialect.get.decorateName(safeColumn),
      dialect.get.decorateName(baseTableName),
      dialect.get.decorateName(Constants.DataIdPropertyName))

    executeSql(command1)

    val command2 = String.format("CREATE INDEX %s ON %S (%s ASC)",
      dialect.get.decorateName("IX_" + column),
      dialect.get.decorateName(dependentTableName),
      dialect.get.decorateName(column))
    executeSql(command2)
  }

  def deleteDependency[TD <: DataObject, TB <: DataObject](typeD: Class[TD], typeB: Class[TB], column: String) {

    if (connection.isEmpty) {
      return
    }

    val dependentTableName: String = English.plural(typeD.getSimpleName)
    val baseTableName: String = English.plural(typeB.getSimpleName)
    val safeColumn = if (column == null) String.format("%sId", typeB.getSimpleName) else column

    val command1: String = String.format("DROP INDEX %s ON %s",
      dialect.get.decorateName("IX_" + safeColumn),
      dialect.get.decorateName(dependentTableName))
    executeSql(command1)
    val command2 = String.format("ALTER TABLE %s DROP CONSTRAINT %s",
      dialect.get.decorateName(dependentTableName),
      dialect.get.decorateName(String.format("FK_%s_%s_%s", dependentTableName, baseTableName, safeColumn)))
    executeSql(command2)
  }

  class UpdatingColumn(val name: String,
                       val nameLower: String,
                       val dataType: Option[DatabaseDataType],
                       val required: Boolean,
                       val sizeLimit: Option[Int],
                       val characterSet: Option[String],
                       val columnType: String,
                       val currentType: Option[String] )

  def getColumnToUpdate(result: ResultSet, columnsToAdd: Map[String, String]): UpdatingColumn = {
    val columnName: String = result.getString(1)
    val columnNameLower = columnName.toLowerCase
    val required: Boolean = !("YES" == result.getString(2))
    val dataType: DatabaseDataType = dialect.get.getDataType(result.getString(3))
    val intValue: Int = result.getInt(4)
    val sizeLimit = if (!result.wasNull && intValue != 65535) Option(intValue) else None
    val charValue = result.getString(5)
    val characterSet = if (result.wasNull) None else Option(charValue)
    val columnType: String = dialect.get.getTypeDeclaration(dataType, sizeLimit, required, characterSet)
    new UpdatingColumn(columnName, columnNameLower, Option(dataType), required, sizeLimit, characterSet, columnType, columnsToAdd.get(columnNameLower))
  }

  def getColumnsToUpdate(tableName: String, columnsToAdd: Map[String, String]) = {
    val statement: Statement = connection.get.createStatement
    try {
      val result: ResultSet = statement.executeQuery(
        "SELECT COLUMN_NAME, IS_NULLABLE, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, CHARACTER_SET_NAME " +
          "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "'")
      try {
        Stream.continually(if (result.next) Option(getColumnToUpdate(result, columnsToAdd)) else None)
          .takeWhile(_.isDefined)
          .map(_.get)
      } finally {
        if (result != null) result.close()
      }
    } finally {
      if (statement != null) statement.close()
    }
  }

  def updateTable[T <: DataObject](`type`: Class[T]) {

    if (connection.isEmpty) {
      return
    }

    val tableName: String = English.plural(`type`.getSimpleName)
    val columnId: String = Constants.DataIdPropertyName.toLowerCase

    val columnsToAdd = TypeHelper.getProperties(`type`).map(p => (p, p.getName.toLowerCase)).filter(_._2 != columnId).map(p => {
      (p._2, getDataType(p._1))
    }).toMap

    val existingColumns = getColumnsToUpdate(tableName, columnsToAdd)

    val columnsToDrop = existingColumns.filter(c => c.nameLower == columnId
      || c.currentType.isEmpty
      || c.dataType.isEmpty
      || c.currentType.get != c.columnType).map(_.name).toSet

    val columnsToLeave = existingColumns.filter(c => !columnsToDrop.contains(c.name)).map(_.name).toSet

    for (column <- columnsToDrop) {
      executeSql(String.format("ALTER TABLE %s DROP COLUMN %s",
        dialect.get.decorateName(tableName),
        dialect.get.decorateName(column)))
    }

    for (entry <- columnsToAdd.filter(c => !columnsToLeave.contains(c._1)).entrySet) {
      executeSql(String.format("ALTER TABLE %s ADD %s %s",
        dialect.get.decorateName(tableName),
        dialect.get.decorateName(entry.getKey), entry.getValue))
    }
  }

  def deleteTable[T <: DataObject](`type`: Class[T]) {
    if (connection == null) {
      return
    }
    val text: String = String.format("DROP TABLE %s", dialect.get.decorateName(English.plural(`type`.getSimpleName)))
    executeSql(text)
  }
}
