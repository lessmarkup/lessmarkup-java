/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.data.migrate

import java.time.OffsetDateTime
import java.util.logging.{Level, Logger}

import com.google.inject.Inject
import com.lessmarkup.dataobjects.MigrationHistory
import com.lessmarkup.framework.helpers.StringHelper
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider, Migration, Migrator}
import com.lessmarkup.interfaces.exceptions.CommonException
import com.lessmarkup.interfaces.module.ModuleProvider

class MigrateEngine @Inject() (moduleProvider: ModuleProvider, domainModelProvider: DomainModelProvider) {

  def execute() {
    val connectionString: String = RequestContextHolder.getContext.getEngineConfiguration.getConnectionString
    if (StringHelper.isNullOrEmpty(connectionString)) {
      return
    }
    execute(connectionString)
  }

  def execute(connectionString: String) {
    if (StringHelper.isNullOrEmpty(connectionString)) {
      throw new CommonException("Connection string is empty")
    }
    val migrator: Migrator = new MigratorImpl(connectionString)
    if (!migrator.checkExists(classOf[MigrationHistory])) {
      migrator.createTable(classOf[MigrationHistory])
    }
    val domainModel: DomainModel = domainModelProvider.create(connectionString)
    try {
      val existingMigrations = domainModel.query
        .from(classOf[MigrationHistory])
        .toList(classOf[MigrationHistory], Option("uniqueId"))
        .map(_.uniqueId)
        .toSet
      for (module <- moduleProvider.getModules) {
        for (migrationType <- module.getInitializer.getMigrations) {
          try {
            val migration: Migration = migrationType.newInstance
            val uniqueId = migration.getId + "_" + migrationType.getName
            if (!existingMigrations.contains(uniqueId)) {
              migration.migrate(migrator)
              val history: MigrationHistory = new MigrationHistory(
                created = OffsetDateTime.now,
                uniqueId = uniqueId,
                moduleType = module.getModuleType,
                id = 0
              )
              domainModel.create(history)
            }
          }
          catch {
            case ex: Any =>
              Logger.getLogger(classOf[MigrateEngine].getName).log(Level.SEVERE, null, ex)
          }
        }
      }
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }
}