package com.lessmarkup.engine.data

import java.sql.{Connection, PreparedStatement, ResultSet, ResultSetMetaData, Timestamp}
import java.time.{OffsetDateTime, ZoneOffset}

import com.lessmarkup.Constants
import com.lessmarkup.engine.data.dialects.{DatabaseLanguageDialect, DatabaseLanguageDialectFactory}
import com.lessmarkup.framework.helpers.{DependencyResolver, PropertyDescriptor, StringHelper, TypeHelper}
import com.lessmarkup.interfaces.data._

import scala.collection.JavaConversions._
import scala.collection.mutable


class QueryBuilderImpl(connection: Connection,
                       commandText: Option[String] = None,
                       select: Option[String] = None,
                       where: Option[String] = None,
                       orderBy: Option[String] = None,
                       limit: Option[String] = None,
                       parameters: List[Any] = List()) extends QueryBuilder {

  private val dialect: DatabaseLanguageDialect = DatabaseLanguageDialectFactory.createDialect(Option(this.connection))

  private def constructCopy(
           commandText: Option[String] = None,
           select: Option[String] = None,
           where: Option[String] = None,
           orderBy: Option[String] = None,
           limit: Option[String] = None,
           parameters: Option[List[Any]] = None) = {

    new QueryBuilderImpl(connection = this.connection,
      commandText = if (commandText.isDefined) Option(this.commandText.getOrElse("") + commandText.get) else this.commandText,
      select = if (select.isDefined) select else this.select,
      where =
        if (where.isDefined)
          if (this.where.isDefined)
            Option(this.where.get + " AND " + where.get)
          else
            where
        else
          this.where,
      orderBy =
        if (orderBy.isDefined)
          if (this.orderBy.isDefined)
            Option(this.orderBy.get + ", " + orderBy.get)
          else
            Option("ORDER BY " + orderBy.get)
        else
          this.orderBy,
      limit =
        if (limit.isDefined)
          limit
        else
          this.limit,
      parameters =
        if (parameters.isDefined)
          this.parameters ::: parameters.get
        else
          this.parameters
    )
  }

  def from[T <: DataObject](dataType: Class[T], name: Option[String]) = {

    val metadata: TableMetadata = MetadataStorage.getMetadata(dataType)
    if (metadata == null) {
      throw new IllegalArgumentException
    }

    val safeName = name.getOrElse("")

    constructCopy(commandText = Option(s" FROM ${dialect.decorateName(metadata.getName)} $safeName"))
  }

  private def anyJoin[T <: DataObject](dataType: Class[T], joinType: String, name: String, on: String): QueryBuilder = {

    val metadata: TableMetadata = MetadataStorage.getMetadata(dataType)
    if (metadata == null) {
      throw new IllegalArgumentException
    }

    constructCopy(commandText = Option(s" $joinType ${dialect.decorateName(metadata.getName)} $name ON $on"))
  }

  private def processStringWithParameters(sql: String, args: Seq[Any]): (List[Any], String) = {
    if (args.isEmpty) {
      return (List(), sql)
    }

    val matchNumber = "$(\\d+)".r
    val matchTable = "$-(\\w+)".r

    val parameters = mutable.ListBuffer[Any]()

    val resultingSql = "($(\\d+))|($-(\\w+))".r.replaceAllIn(sql, _ match {
      case matchTable(table) =>
        MetadataStorage.getMetadata(table).getName
      case matchNumber(num) =>
        val parameterIndex = num.toInt
        parameters += args(parameterIndex)
        "?"
    })

    (parameters.toList, resultingSql)
  }

  def join[T <: DataObject](dataType: Class[T], name: String, on: String) = anyJoin(dataType, "", name, on)
  def leftJoin[T <: DataObject](dataType: Class[T], name: String, on: String) = anyJoin(dataType, "LEFT ", name, on)
  def rightJoin[T <: DataObject](dataType: Class[T], name: String, on: String) = anyJoin(dataType, "RIGHT ", name, on)

  def where(filter: String, args: Any*): QueryBuilder = {

    val ret = processStringWithParameters(filter, args)
    constructCopy(where = Option(ret._2), parameters = Option(ret._1))
  }

  def whereIds(ids: Iterator[Long]): QueryBuilder = {
    if (ids.isEmpty) {
      throw new IllegalArgumentException
    }

    constructCopy(where = Option(s"${dialect.decorateName(Constants.DataIdPropertyName)} IN (${ids.map(_.toString).mkString(",")})"))
  }

  def whereId(id: Long) = where(Constants.DataIdPropertyName + " = $", id)

  def orderBy(column: String): QueryBuilder =
    constructCopy(orderBy = Option(dialect.decorateName(column)))

  def orderByDescending(column: String): QueryBuilder =
    constructCopy(orderBy = Option(dialect.decorateName(column) + " DESC"))

  def groupBy(column: String): QueryBuilder =
    constructCopy(commandText = Option(s" GROUP BY ${dialect.decorateName(column)} "))

  def limit(from: Int, count: Int): QueryBuilder =
    constructCopy(limit = Option(dialect.paging(from, count)))

  def find[T <: DataObject](dataType: Class[T], id: Long): Option[T] =
    (if (commandText.isEmpty) from(dataType) else this)
      .where(dialect.decorateName(Constants.DataIdPropertyName) + " = $", id).first(dataType, None)

  private def getSql: String = {

    val select = this.select.getOrElse("")
    val commandText = this.commandText.getOrElse("")
    val where = if (this.where.isDefined) " WHERE " + this.where.get else ""
    val orderBy = this.orderBy.getOrElse("")
    val limit = this.limit.getOrElse("")

    s"SELECT $select $commandText $where $orderBy $limit"
  }

  private def prepareStatement(sql: String, parameters: List[Any]): PreparedStatement = {
    val localStatement: PreparedStatement = this.connection.prepareStatement(sql)
    for ((param, index) <- parameters.view.zipWithIndex) {
      localStatement.setObject(index, param)
    }
    localStatement
  }

  private def readDataValue[T](property: PropertyDescriptor, dataObject: T, resultSet: ResultSet, index: Int) {
    if (property.getType == classOf[OptionInt]) {
      val intValue = resultSet.getInt(index)
      property.setValue(dataObject, if (resultSet.wasNull()) new OptionInt() else new OptionInt(intValue))
    }
    else if (property.getType == classOf[OptionLong]) {
      val longValue = resultSet.getLong(index)
      property.setValue(dataObject, if (resultSet.wasNull()) new OptionLong() else new OptionLong(longValue))
    }
    else if (property.getType == classOf[OptionDouble]) {
      val doubleValue = resultSet.getDouble(index)
      property.setValue(dataObject, if (resultSet.wasNull()) new OptionDouble() else new OptionDouble(doubleValue))
    }
    else if (property.getType == classOf[OptionBool]) {
      val booleanValue = resultSet.getBoolean(index)
      property.setValue(dataObject, if (resultSet.wasNull()) new OptionBool() else new OptionBool(booleanValue))
    }
    else if (property.getType == classOf[OffsetDateTime]) {
      val timestamp: Timestamp = resultSet.getTimestamp(index)
      property.setValue(dataObject, if (resultSet.wasNull()) null.asInstanceOf[OffsetDateTime] else {
        OffsetDateTime.ofInstant(timestamp.toInstant, ZoneOffset.UTC)
      })
    }
    else {
      val value: AnyRef = resultSet.getObject(index)
      if (resultSet.wasNull) {
        property.setValue(dataObject, null)
      }
      else {
        property.setValue(dataObject, value)
      }
    }
  }

  private def readDataObject[T](dataType: Class[T], resultSetMetadata: ResultSetMetaData, metadata: TableMetadata, resultSet: ResultSet) : T = {

    val dataObject: T = dataType.newInstance

    for (columnIndex <- 1 to resultSetMetadata.getColumnCount) {
      val columnName: String = resultSetMetadata.getColumnName(columnIndex)
      val column: Option[PropertyDescriptor] = metadata.getColumns.get(columnName)

      if (column.isDefined) {
        readDataValue(column.get, dataObject, resultSet, columnIndex)
      }
    }

    dataObject
  }

  private def executeOnDataObjectWithLimit[T <: DataObject](dataType: Class[T], sql: String, limit: Option[Int], args: Any*): List[T] = {

    val sqlAndParameters = processStringWithParameters(sql, args)
    val metadata: TableMetadata = MetadataStorage.getMetadata(dataType)

    val statement: PreparedStatement = prepareStatement(sqlAndParameters._2, sqlAndParameters._1)
    val resultSet: ResultSet = statement.executeQuery
    try {
      val resultSetMetadata: ResultSetMetaData = resultSet.getMetaData

      val stream = Stream.continually(if (resultSet.next) Option(readDataObject(dataType, resultSetMetadata, metadata, resultSet)) else None)
        .takeWhile(_.isDefined)
        .map(_.get)

      if (limit.isDefined) {
        stream.take(limit.get).toList
      } else {
        stream.toList
      }
    } finally {
      if (statement != null) statement.close()
      if (resultSet != null) resultSet.close()
    }
  }

  private def readRegularObject[T](dataType: Class[T], resultSetMetadata: ResultSetMetaData, properties: Map[String, PropertyDescriptor], resultSet: ResultSet) : T = {

    val isDataObject: Boolean = classOf[DataObject].isAssignableFrom(dataType)

    val dataObject = if (isDataObject) {
      dataType.newInstance
    } else {
      DependencyResolver.resolve(dataType)
    }

    for (columnIndex <- 1 to resultSetMetadata.getColumnCount) {
      val columnName: String = resultSetMetadata.getColumnName(columnIndex)
      val property: Option[PropertyDescriptor] = properties.get(StringHelper.toJsonCase(columnName))

      if (property.isDefined) {
        readDataValue(property.get, dataObject, resultSet, columnIndex)
      }
    }

    dataObject
  }


  private def executeOnRegularObjectWithLimit[T](dataType: Class[T], sql: String, limit: Option[Int], args: Any*): List[T] = {

    val sqlAndParameters = processStringWithParameters(sql, args)
    val properties = TypeHelper.getProperties(dataType).toList.map(p => (p.getName, p)).toMap

    val statement: PreparedStatement = prepareStatement(sqlAndParameters._2, sqlAndParameters._1)
    val resultSet: ResultSet = statement.executeQuery
    try {
      val resultSetMetadata: ResultSetMetaData = resultSet.getMetaData

      val stream = Stream.continually(if (resultSet.next) Option(readRegularObject(dataType, resultSetMetadata, properties, resultSet)) else None)
        .takeWhile(_.isDefined)
        .map(_.get)

      if (limit.isDefined) {
        stream.take(limit.get).toList
      } else {
        stream.toList
      }
    } finally {
      if (statement != null) statement.close()
      if (resultSet != null) resultSet.close()
    }
  }

  def execute[T <: DataObject](dataType: Class[T], sql: String, args: Any*): List[T] = {
    executeOnDataObjectWithLimit(dataType, sql, None, args: _*)
  }

  def executeNonQuery(sql: String, args: Any*): Boolean = {
    val localStatement: PreparedStatement = prepareStatement(sql, parameters)
    try {
      localStatement.execute
    } finally {
      if (localStatement != null) localStatement.close()
    }
  }

  def executeScalar[T](dataType: Class[T], sql: String, args: Any*): Option[T] = {
    val localStatement: PreparedStatement = prepareStatement(sql, parameters)
    try {
      val resultSet: ResultSet = localStatement.executeQuery
      try {
        if (!resultSet.next) {
          None
        } else {
          Option(resultSet.getObject(1, dataType))
        }
      } finally {
        if (resultSet != null) resultSet.close()
      }
    } finally {
      if (localStatement != null) localStatement.close()
    }
  }

  def toList[T](dataType: Class[T], selectText: Option[String]): List[T] = {
    (if (this.select.isEmpty) constructCopy(select = selectText) else this)
      .executeOnRegularObjectWithLimit(dataType, getSql, None)
  }

  def toIdList: List[Long] = {
    val statement: PreparedStatement = prepareStatement(getSql, parameters)
    try {
      val resultSet: ResultSet = statement.executeQuery
      try {

        Stream.continually(if (resultSet.next) Option(resultSet.getLong(Constants.DataIdPropertyName)) else None)
          .takeWhile(_.isDefined)
          .map(_.get)
          .toList

      } finally {
        if (resultSet != null) resultSet.close()
      }
    } finally {
      if (statement != null) statement.close()
    }
  }

  def count: Int = {
    constructCopy(select = Option("COUNT(*)"))
      .executeScalar(classOf[Integer], getSql).asInstanceOf[Long].toInt
  }

  def first[T](dataType: Class[T], selectText: Option[String]): Option[T] = {
    (if (selectText.isDefined) constructCopy(select = selectText) else this)
      .executeOnRegularObjectWithLimit(dataType, getSql, Option(1))
      .headOption
  }

  def createNew: QueryBuilder = constructCopy()

  def deleteFrom[T <: DataObject](dataType: Class[T], filter: String, args: Any*): Boolean = {

    val sqlWithParameters = processStringWithParameters(filter, args)
    val metadata: TableMetadata = MetadataStorage.getMetadata(dataType)

    if (metadata == null) {
      throw new IllegalArgumentException
    }

    val sql: String = String.format("DELETE FROM %s WHERE %s", dialect.decorateName(metadata.getName), sqlWithParameters._2)

    val statement: PreparedStatement = prepareStatement(sql, sqlWithParameters._1)
    try {
      statement.execute
    } finally {
      if (statement != null) statement.close()
    }
  }
}
