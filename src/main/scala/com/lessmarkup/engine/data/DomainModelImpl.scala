package com.lessmarkup.engine.data

import java.sql._
import java.time.OffsetDateTime

import com.lessmarkup.Constants
import com.lessmarkup.engine.data.dialects.{DatabaseLanguageDialect, DatabaseLanguageDialectFactory}
import com.lessmarkup.framework.helpers.{PropertyDescriptor, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.data._

object DomainModelImpl {

  private def getConnectionString: String = {
    RequestContextHolder.getContext.getEngineConfiguration.getConnectionString
  }
}

class DomainModelImpl(connectionString: Option[String], inTransaction: Boolean) extends DomainModel {

  private val connection: Option[Connection] = if (inTransaction) createConnectionWithTransaction else createConnection
  private val dialect: DatabaseLanguageDialect = DatabaseLanguageDialectFactory.createDialect(connection)

  def query: QueryBuilder = {
    if (this.connection.isEmpty) {
      new QueryBuilderStubImpl
    } else {
      new QueryBuilderImpl(this.connection.get)
    }
  }

  private def createConnectionWithTransaction: Option[Connection] = {
    val actualConnectionString: String = connectionString.getOrElse(DomainModelImpl.getConnectionString)
    if (StringHelper.isNullOrEmpty(actualConnectionString)) {
      None
    } else {
      val connection = ConnectionManager.getConnection(actualConnectionString)
      connection.setAutoCommit(false)
      Option(connection)
    }
  }

  private def createConnection: Option[Connection] = {
    val actualConnectionString: String = connectionString.getOrElse(DomainModelImpl.getConnectionString)
    if (StringHelper.isNullOrEmpty(actualConnectionString)) {
      None
    } else {
      Option(ConnectionManager.getConnection(actualConnectionString))
    }
  }

  def completeTransaction() {

    if (connection.isEmpty) {
      return
    }

    connection.get.commit()
  }

  private def updateDataValue(property: PropertyDescriptor, value: AnyRef, statement: PreparedStatement, columnIndex: Int) {
    if (property.getType == classOf[OptionInt]) {
      val valueInt = value.asInstanceOf[OptionInt]
      if (valueInt.isDefined) {
        statement.setObject(columnIndex, valueInt.get)
      }
      else {
        statement.setNull(columnIndex, Types.INTEGER)
      }
    }
    else if (property.getType == classOf[OptionLong]) {
      val valueLong = value.asInstanceOf[OptionLong]
      if (valueLong.isDefined) {
        statement.setObject(columnIndex, valueLong.get)
      }
      else {
        statement.setNull(columnIndex, Types.BIGINT)
      }
    }
    else if (property.getType == classOf[OptionBool]) {
      val valueBool = value.asInstanceOf[OptionBool]
      if (valueBool.isDefined) {
        statement.setObject(columnIndex, valueBool.get)
      }
      else {
        statement.setNull(columnIndex, Types.BIT)
      }
    }
    else if (property.getType == classOf[OptionDouble]) {
      val valueDouble: OptionDouble = value.asInstanceOf[OptionDouble]
      if (valueDouble.isDefined) {
        statement.setObject(columnIndex, valueDouble.get)
      }
      else {
        statement.setNull(columnIndex, Types.DOUBLE)
      }
    }
    else if (property.getType == classOf[OffsetDateTime]) {
      if (value == null) {
        statement.setNull(columnIndex, Types.TIMESTAMP_WITH_TIMEZONE)
      }
      else {
        val timestamp: Timestamp = Timestamp.from(value.asInstanceOf[OffsetDateTime].toInstant)
        statement.setTimestamp(columnIndex, timestamp)
      }
    }
    else {
      if (value == null) {
        statement.setNull(columnIndex, Types.JAVA_OBJECT)
      }
      else {
        statement.setObject(columnIndex, value)
      }
    }
  }

  def update[T <: DataObject](dataObject: T): Boolean = {
    if (connection.isEmpty) {
      return false
    }

    val metadata: TableMetadata = MetadataStorage.getMetadata(dataObject.getClass)

    val columnsWithoutId = metadata.getColumns.values
      .filter(_.getName != Constants.DataIdPropertyName).toList

    val command =
      String.format("UPDATE %s SET ", this.dialect.decorateName(metadata.getName)) +
      columnsWithoutId
        .map(c => s"${dialect.decorateName(c.getName)} = ?")
        .mkString(", ") +
      String.format(" WHERE %s = ?", this.dialect.decorateName(Constants.DataIdPropertyName))

    val statement: PreparedStatement = connection.get.prepareStatement(command.toString)
    try {
      for ((column, index) <- columnsWithoutId.view.zipWithIndex) {
        updateDataValue(column, column.getValue(dataObject), statement, index)
      }
      statement.setLong(columnsWithoutId.size, dataObject.getId)
      statement.executeUpdate != 0
    } finally {
      if (statement != null) statement.close()
    }
  }

  def create[T <: DataObject](dataObject: T): Boolean = {
    if (connection == null) {
      return false
    }

    val metadata: TableMetadata = MetadataStorage.getMetadata(dataObject.getClass)

    val columnsWithoutId = metadata.getColumns.values
      .filter(_.getName != Constants.DataIdPropertyName).toList

    val names = columnsWithoutId.map(c => dialect.decorateName(c.getName)).mkString(", ")
    val values = columnsWithoutId.map(c => "?").mkString(", ")
    val command: String = String.format("INSERT INTO %s (%s) VALUES (%s)", this.dialect.decorateName(metadata.getName), names.toString, values.toString)

    val statement: PreparedStatement = connection.get.prepareStatement(command, Statement.RETURN_GENERATED_KEYS)
    try {
      for ((column, index) <- columnsWithoutId.view.zipWithIndex) {
        updateDataValue(column, column.getValue(dataObject), statement, index)
      }
      val createdRecords: Int = statement.executeUpdate
      if (createdRecords == 0) {
        return false
      }
      val generatedKeys: ResultSet = statement.getGeneratedKeys
      try {
        if (!generatedKeys.next) {
          return false
        }
        dataObject.setId(generatedKeys.getLong(1))
      } finally {
        if (generatedKeys != null) generatedKeys.close()
      }
      true
    } finally {
      if (statement != null) statement.close()
    }
  }

  def delete[T <: DataObject](`type`: Class[T], id: Long): Boolean = {
    if (connection == null) {
      return false
    }
    val metadata: TableMetadata = MetadataStorage.getMetadata(`type`)
    val statement: Statement = this.connection.get.createStatement
    try {
      statement.execute(s"DELETE FROM ${dialect.decorateName(metadata.getName)} WHERE ${dialect.decorateName(Constants.DataIdPropertyName)}=$id")
    } finally {
      if (statement != null) statement.close()
    }
  }

  def close() {
    if (connection.isEmpty) {
      return
    }

    this.connection.get.close()
  }
}