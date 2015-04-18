package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.userinterface.model.global.DatabaseConfigurationModel;
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DatabaseConfigurationNodeHandler extends DialogNodeHandler<DatabaseConfigurationModel> {

    @Autowired
    public DatabaseConfigurationNodeHandler(DataCache dataCache) {
        super(dataCache, DatabaseConfigurationModel.class);
    }

    @Override
    protected DatabaseConfigurationModel loadObject() {
        DatabaseConfigurationModel model = DependencyResolver.resolve(DatabaseConfigurationModel.class);
        model.setDatabase(RequestContextHolder.getContext().getEngineConfiguration().getConnectionString());
        return model;
    }

    @Override
    protected String saveObject(DatabaseConfigurationModel changedObject) {
        return changedObject.save();
    }
}
