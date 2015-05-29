/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

trait QueryBuilder {

  def from[T <: DataObject](dataType: Class[T], name: Option[String]): QueryBuilder

  def fromJava[T <: DataObject](dataType: Class[T], name: String): QueryBuilder = from(dataType, Option(name))

  def from[T <: DataObject](dataType: Class[T]) = fromJava(dataType, null)

  def join[T <: DataObject](dataType: Class[T], name: String, on: String): QueryBuilder

  def leftJoin[T <: DataObject](dataType: Class[T], name: String, on: String): QueryBuilder

  def rightJoin[T <: DataObject](dataType: Class[T], name: String, on: String): QueryBuilder

  def where(filter: String, args: Any*): QueryBuilder

  def whereId(id: Long): QueryBuilder

  def whereIds(ids: Seq[Long]): QueryBuilder

  def orderBy(column: String): QueryBuilder

  def orderByDescending(column: String): QueryBuilder

  def groupBy(column: String): QueryBuilder

  def limit(from: Int, count: Int): QueryBuilder

  def find[T <: DataObject](dataType: Class[T], id: Long): Option[T]

  def execute[T <: DataObject](dataType: Class[T], sql: String, args: Any*): Seq[T]

  def executeNonQuery(sql: String, args: Any*): Boolean

  def executeScalar[T](dataType: Class[T], sql: String, args: Any*): Option[T]

  def toList[T <: AnyRef](dataType: Class[T], selectText: Option[String]): Seq[T]

  def toList[T <: AnyRef](dataType: Class[T]): Seq[T] = toList(dataType, None)

  def toIdList: Seq[Long]

  def count: Int

  def first[T <: AnyRef](dataType: Class[T], selectText: Option[String]): Option[T]

  def createNew: QueryBuilder

  def deleteFrom[T <: DataObject](dataType: Class[T], filter: String, args: Any*): Boolean
}
