/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.recordmodel

trait EditableModelCollection[T <: RecordModel[_]] extends ModelCollection[T] {
  def createRecord: T

  def addRecord(record: T)

  def updateRecord(record: T)

  def deleteRecords(recordIds: Seq[Long]): Boolean

  def isDeleteOnly: Boolean = false
}
