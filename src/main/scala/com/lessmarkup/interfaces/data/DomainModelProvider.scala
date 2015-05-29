/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

trait DomainModelProvider {
  def create: DomainModel
  def create(connectionString: String): DomainModel
  def createWithTransaction: DomainModel
  def getCollectionId(collectionType: Class[_]): Option[Int]
  def initialize()
}