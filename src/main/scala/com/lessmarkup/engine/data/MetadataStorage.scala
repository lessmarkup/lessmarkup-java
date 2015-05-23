package com.lessmarkup.engine.data

import com.lessmarkup.interfaces.data.DataObject

object MetadataStorage {

  private val collectionTypeToId: java.util.HashMap[Class[_], Integer] = new java.util.HashMap[Class[_], Integer]
  private val collectionIdToType: java.util.HashMap[Integer, Class[_]] = new java.util.HashMap[Integer, Class[_]]
  private val tableMetadatas: java.util.HashMap[Class[_], TableMetadata] = new java.util.HashMap[Class[_], TableMetadata]
  private val stringToMetadata: java.util.HashMap[String, TableMetadata] = new java.util.HashMap[String, TableMetadata]
  private var collectionIdCounter: Int = 1

  def getCollectionId(collectionType: Class[_]): Option[Int] = {
    val collectionId: Integer = collectionTypeToId.get(collectionType)
    if (collectionId == null) None
    else Option(collectionId)
  }

  def getCollectionType(collectionId: Int): Class[_] = {
    val collectionType: Class[_] = collectionIdToType.get(collectionId)
    if (collectionType == null) {
      throw new ArrayIndexOutOfBoundsException
    }
    collectionType
  }

  def getMetadata[T <: DataObject](`type`: Class[T]): TableMetadata = {
    tableMetadatas.get(`type`)
  }

  def getMetadata(tableName: String): TableMetadata = {
    stringToMetadata.get(tableName)
  }

  def registerDataType[T <: DataObject](collectionType: Class[T]) {
    collectionTypeToId.put(collectionType, collectionIdCounter)
    collectionIdToType.put(collectionIdCounter, collectionType)
    collectionIdCounter += 1
    val metadata: TableMetadata = new TableMetadata(collectionType)
    tableMetadatas.put(collectionType, metadata)
    stringToMetadata.put(metadata.getName, metadata)
  }

}
