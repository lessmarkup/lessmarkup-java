/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

trait DomainModel extends AutoCloseable {
  def query: QueryBuilder
  def completeTransaction()
  def update[T <: DataObject](dataObject: T): Boolean
  def create[T <: DataObject](dataObject: T): Boolean
  def delete[T <: DataObject](dataType: Class[T], id: Long): Boolean
  def close()
}