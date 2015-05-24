package com.lessmarkup.userinterface.model.global;

import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.engine.data.ConnectionManager;
import com.lessmarkup.engine.data.migrate.MigrateEngine;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.annotations.InputField;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.structure.ActionAccess;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import com.lessmarkup.interfaces.system.EngineConfiguration;

import java.sql.SQLException;

public class DatabaseConfigurationModel extends RecordModel<DatabaseConfigurationModel> {
    private String database;

    public DatabaseConfigurationModel() {
        super(TextIds.DATABASE_CONFIGURATION);
    }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.CONNECTION_STRING, required = true)
    public void setDatabase(String database) { this.database = database; }
    public String getDatabase() { return this.database; }

    private void checkConnection() throws SQLException, ClassNotFoundException {
        ConnectionManager.getConnection(this.database).getSchema();
    }

    @ActionAccess(minimumAccess = NodeAccessType.READ)
    public String save() {
        try {

            checkConnection();

            MigrateEngine migrateEngine = DependencyResolver.resolve(MigrateEngine.class);
            migrateEngine.execute(this.database);

            EngineConfiguration engineConfiguration = RequestContextHolder.getContext().getEngineConfiguration();
            engineConfiguration.setConnectionString(this.database);

        } catch (Exception e) {
            String errorMessage = LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.DATABASE_CHANGE_ERROR, StringHelper.getMessage(e));
            throw new CommonException(errorMessage);
        }

        return LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.DATABASE_CHANGE_SUCCESS);
    }
}
