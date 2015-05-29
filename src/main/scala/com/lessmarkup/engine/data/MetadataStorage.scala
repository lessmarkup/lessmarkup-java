/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data

import com.lessmarkup.interfaces.data.DataObject
import scala.collection.mutable

object MetadataStorage {

  private val collectionTypeToId: mutable.Map[Class[_], Integer] = mutable.Map()
  private val collectionIdToType: mutable.Map[Integer, Class[_]] = mutable.Map()
  private val tableMetadatas: mutable.Map[Class[_], TableMetadata] = mutable.Map()
  private val stringToMetadata: mutable.Map[String, TableMetadata] = mutable.Map()
  private var collectionIdCounter: Int = 1

  def getCollectionId(collectionType: Class[_]): Option[Int] = {
    val collectionId = collectionTypeToId.get(collectionType)
    if (collectionId.isEmpty) None
    else Option(collectionId.get)
  }

  def getCollectionType(collectionId: Int): Class[_] = {
    val collectionType = collectionIdToType.get(collectionId)
    if (collectionType.isEmpty) {
      throw new ArrayIndexOutOfBoundsException
    }
    collectionType.get
  }

  def getMetadata[T <: DataObject](dataType: Class[T]): Option[TableMetadata] = {
    tableMetadatas.get(dataType)
  }

  def getMetadata(tableName: String): Option[TableMetadata] = {
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
