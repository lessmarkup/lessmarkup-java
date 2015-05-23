package com.lessmarkup.interfaces.data

trait DomainModel extends AutoCloseable {
  def query: QueryBuilder
  def completeTransaction()
  def update[T <: DataObject](dataObject: T): Boolean
  def create[T <: DataObject](dataObject: T): Boolean
  def delete[T <: DataObject](dataType: Class[T], id: Long): Boolean
  def close()
}