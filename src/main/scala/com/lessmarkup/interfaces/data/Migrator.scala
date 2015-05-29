/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

trait Migrator {
  def executeSql(sql: String)
  def checkExists[T <: DataObject](`type`: Class[T]): Boolean
  def createTable[T <: DataObject](`type`: Class[T])
  def addDependency[TD <: DataObject, TB <: DataObject](typeD: Class[TD], typeB: Class[TB]) {
    addDependency(typeD, typeB, null)
  }
  def addDependency[TD <: DataObject, TB <: DataObject](typeD: Class[TD], typeB: Class[TB], column: String)
  def deleteDependency[TD <: DataObject, TB <: DataObject](typeD: Class[TD], typeB: Class[TB]) {
    deleteDependency(typeD, typeB, null)
  }
  def deleteDependency[TD <: DataObject, TB <: DataObject](typeD: Class[TD], typeB: Class[TB], column: String)
  def updateTable[T <: DataObject](`type`: Class[T])
  def deleteTable[T <: DataObject](`type`: Class[T])
}