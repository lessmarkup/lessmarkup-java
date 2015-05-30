/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.testutilities

import com.lessmarkup.engine.data.ConnectionManager
import com.lessmarkup.engine.data.migrate.MigratorImpl
import com.lessmarkup.engine.module.ModuleProviderImpl

object StaticTestEnvironmentHolder {
  lazy val testEnvironment = new StaticTestEnvironment
}

class StaticTestEnvironment {
  val moduleProvider = new ModuleProviderImpl(onlySystem = true)
  val domainModelProvider = {
    val connection = ConnectionManager.getConnection(DatabaseFactory.getConnectionString)
    try {

      val migrator = new MigratorImpl(Option(connection))

      for (
        module <- moduleProvider.getModules;
        migrationType <- module.getInitializer.getMigrations;
        migration = migrationType.newInstance()
      ) {
        migration.migrate(migrator)
      }

      val provider = new TestDomainModelProvider(moduleProvider, DatabaseFactory.connection)
      provider.initialize()
      provider

    } finally {
      connection.close()
    }
  }
}
