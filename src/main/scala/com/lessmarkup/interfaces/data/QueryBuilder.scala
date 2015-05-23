package com.lessmarkup.interfaces.data

import scala.collection.JavaConversions._

trait QueryBuilder {

  def from[T <: DataObject](dataType: Class[T], name: Option[String]): QueryBuilder

  def fromJava[T <: DataObject](dataType: Class[T], name: String): QueryBuilder = from(dataType, Option(name))

  def from[T <: DataObject](dataType: Class[T]) = fromJava(dataType, null)

  def join[T <: DataObject](`type`: Class[T], name: String, on: String): QueryBuilder

  def leftJoin[T <: DataObject](dataType: Class[T], name: String, on: String): QueryBuilder

  def rightJoin[T <: DataObject](dataType: Class[T], name: String, on: String): QueryBuilder

  def where(filter: String, args: Any*): QueryBuilder

  @Deprecated
  def whereJava(filter: String, args: java.util.List[Any]): QueryBuilder = {
    where(filter, args.toList: _*)
  }

  def whereId(id: Long): QueryBuilder

  def whereIds(ids: Iterator[Long]): QueryBuilder

  def whereIds(ids: List[Long]): QueryBuilder = whereIds(ids.iterator)

  @Deprecated
  def whereIdsJava(ids: java.util.Collection[java.lang.Long]): QueryBuilder = whereIds(ids.toList.map(_.toLong))

  def orderBy(column: String): QueryBuilder

  def orderByDescending(column: String): QueryBuilder

  def groupBy(column: String): QueryBuilder

  def limit(from: Int, count: Int): QueryBuilder

  def find[T <: DataObject](dataType: Class[T], id: Long): Option[T]

  @Deprecated
  def findJava[T <: DataObject](dataType: Class[T], id: Long) = find(dataType, id).get

  @Deprecated
  def findOrDefaultJava[T <: DataObject](dataType: Class[T], id: Long) =
    find(dataType, id).getOrElse(null.asInstanceOf[T])

  @Deprecated
  def executeJava[T <: DataObject](dataType: Class[T], sql: String, args: java.util.List[Any]) =
    new java.util.LinkedList(execute(dataType, sql, args.toList: _*))

  def execute[T <: DataObject](dataType: Class[T], sql: String, args: Any*): List[T]

  def executeNonQuery(sql: String, args: Any*): Boolean

  def executeScalar[T](dataType: Class[T], sql: String, args: Any*): Option[T]

  @Deprecated
  def executeScalarJava[T](dataType: Class[T], sql: String, args: java.util.List[Any]): T =
    executeScalar(dataType, sql, args.toList: _*).getOrElse(null.asInstanceOf[T])

  def toList[T](dataType: Class[T], selectText: Option[String]): List[T]

  def toList[T](dataType: Class[T]): List[T] = toList(dataType, None)

  @Deprecated
  def toListJava[T](dataType: Class[T]): java.util.List[T] = toListJava(dataType, null)

  @Deprecated
  def toListJava[T](dataType: Class[T], selectText: String): java.util.List[T] =
    new java.util.LinkedList(toList(dataType, Option(selectText)))

  @Deprecated
  def toIdListJava: java.util.List[java.lang.Long] = new java.util.LinkedList(toIdList.map(_.asInstanceOf[java.lang.Long]))

  def toIdList: List[Long]

  def count: Int

  def first[T](dataType: Class[T], selectText: Option[String]): Option[T]

  @Deprecated
  def firstJava[T](dataType: Class[T], selectText: String): T = first(dataType, Option(selectText)).get

  @Deprecated
  def firstJava[T](dataType: Class[T]): T = firstJava(dataType, null)

  @Deprecated
  def firstOrDefaultJava[T](dataType: Class[T], selectText: String): T = first(dataType, Option(selectText)).getOrElse(null.asInstanceOf[T])

  @Deprecated
  def firstOrDefaultJava[T](dataType: Class[T]): T = firstOrDefaultJava(dataType, null)

  def createNew: QueryBuilder

  def deleteFrom[T <: DataObject](dataType: Class[T], filter: String, args: Any*): Boolean

  @Deprecated
  def deleteFromJava[T <: DataObject](dataType: Class[T], filter: String, args: java.util.List[Any]): Boolean = {
    deleteFrom(dataType, filter, args.toList: _*)
  }
}
