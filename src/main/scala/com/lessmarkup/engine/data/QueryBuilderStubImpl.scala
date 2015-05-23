package com.lessmarkup.engine.data

import com.lessmarkup.interfaces.data.DataObject
import com.lessmarkup.interfaces.data.QueryBuilder

class QueryBuilderStubImpl extends QueryBuilder {
  override def from[T <: DataObject](dataType: Class[T], name: Option[String]): QueryBuilder = this

  override def limit(from: Int, count: Int): QueryBuilder = this

  override def count: Int = 0

  override def deleteFrom[T <: DataObject](dataType: Class[T], filter: String, args: Any*): Boolean = false

  override def createNew: QueryBuilder = this

  override def toIdList: List[Long] = List()

  override def leftJoin[T <: DataObject](dataType: Class[T], name: String, on: String): QueryBuilder = this

  override def orderByDescending(column: String): QueryBuilder = this

  override def executeScalar[T](dataType: Class[T], sql: String, args: Any*): Option[T] = None

  override def join[T <: DataObject](`type`: Class[T], name: String, on: String): QueryBuilder = this

  override def executeNonQuery(sql: String, args: Any*): Boolean = false

  override def whereIds(ids: Iterator[Long]): QueryBuilder = this

  override def execute[T <: DataObject](dataType: Class[T], sql: String, args: Any*): List[T] = List()

  override def groupBy(column: String): QueryBuilder = this

  override def toList[T](dataType: Class[T], selectText: Option[String]): List[T] = List()

  override def rightJoin[T <: DataObject](dataType: Class[T], name: String, on: String): QueryBuilder = this

  override def orderBy(column: String): QueryBuilder = this

  override def where(filter: String, args: Any*): QueryBuilder = this

  override def find[T <: DataObject](dataType: Class[T], id: Long): Option[T] = None

  override def first[T](dataType: Class[T], selectText: Option[String]): Option[T] = None

  override def whereId(id: Long): QueryBuilder = this
}
