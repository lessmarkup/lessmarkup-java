/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.testutilities

import java.sql.Connection

import com.lessmarkup.engine.data.DomainModelProviderImpl
import com.lessmarkup.interfaces.data.DomainModel
import com.lessmarkup.interfaces.module.ModuleProvider

class TestDomainModelProvider(moduleProvider: ModuleProvider, connection: Connection) extends DomainModelProviderImpl(moduleProvider) {

  override def create: DomainModel = new TestDomainModelImpl(connection)

  override def create(connectionString: String): DomainModel = new TestDomainModelImpl(connection)

  override def createWithTransaction: DomainModel = new TestDomainModelImpl(connection)
}
