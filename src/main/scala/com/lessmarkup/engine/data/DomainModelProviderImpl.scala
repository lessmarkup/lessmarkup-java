/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data

import com.google.inject.Inject
import com.lessmarkup.interfaces.annotations.Implements
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.module.ModuleProvider

@Implements(classOf[DomainModelProvider])
class DomainModelProviderImpl @Inject() (moduleProvider: ModuleProvider) extends DomainModelProvider {

  def initialize() {
    for (
      module <- moduleProvider.getModules;
      dataObject <- module.getInitializer.getDataObjectTypes
    ) {
        MetadataStorage.registerDataType(dataObject)
    }
  }

  def create: DomainModel = {
    new DomainModelImpl(scala.Option.empty, false)
  }

  def create(connectionString: String): DomainModel = {
    new DomainModelImpl(scala.Option.apply(connectionString), false)
  }

  def createWithTransaction: DomainModel = {
    new DomainModelImpl(scala.Option.empty, true)
  }

  def getCollectionId(collectionType: Class[_]): Option[Int] = {
    MetadataStorage.getCollectionId(collectionType)
  }
}
