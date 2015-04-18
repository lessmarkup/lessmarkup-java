/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.data.migrate;

import com.lessmarkup.dataobjects.MigrationHistory;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.data.Migration;
import com.lessmarkup.interfaces.data.Migrator;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Scope("prototype")
public class MigrateEngine {
    
    private final ModuleProvider moduleProvider;
    private final DomainModelProvider domainModelProvider;

    @Autowired
    public MigrateEngine(ModuleProvider moduleProvider, DomainModelProvider domainModelProvider) {
        this.moduleProvider = moduleProvider;
        this.domainModelProvider = domainModelProvider;
    }

    public void execute() {
        String connectionString = RequestContextHolder.getContext().getEngineConfiguration().getConnectionString();
        if (StringHelper.isNullOrEmpty(connectionString)) {
            return;
        }
        
        execute(connectionString);
    }
    
    public void execute(String connectionString) {
        
        if (StringHelper.isNullOrEmpty(connectionString)) {
            throw new CommonException("Connection string is empty");
        }
        
        Migrator migrator = new MigratorImpl(connectionString);
        
        if (!migrator.checkExists(MigrationHistory.class)) {
            migrator.createTable(MigrationHistory.class);
        }
        
        try (DomainModel domainModel = domainModelProvider.create(connectionString)) {
            Set<String> existingMigrations = new HashSet<>();
            domainModel.query()
                    .from(MigrationHistory.class)
                    .toList(MigrationHistory.class, "uniqueId")
                    .forEach(h -> existingMigrations.add(h.getUniqueId()));
            
            for (ModuleConfiguration module : moduleProvider.getModules()) {
                for (Class<? extends Migration> migrationType : module.getInitializer().getMigrations()) {
                    try {
                        Migration migration = migrationType.newInstance();
                        String uniqueId = migration.getId() + "_" + migrationType.getName();
                        if (existingMigrations.contains(uniqueId)) {
                            continue;
                        }
                        migration.migrate(migrator);
                        MigrationHistory history = new MigrationHistory();
                        history.setCreated(OffsetDateTime.now());
                        history.setUniqueId(uniqueId);
                        history.setModuleType(module.getModuleType());
                        domainModel.create(history);
                    } catch (InstantiationException | IllegalAccessException ex) {
                        Logger.getLogger(MigrateEngine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}
