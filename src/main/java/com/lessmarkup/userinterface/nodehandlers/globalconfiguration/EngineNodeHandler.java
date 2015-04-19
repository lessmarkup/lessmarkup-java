package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.userinterface.model.global.EngineConfigurationModel;
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@ConfigurationHandler(titleTextId = TextIds.ENGINE_CONFIGURATION)
public class EngineNodeHandler extends DialogNodeHandler<EngineConfigurationModel> {

    @Autowired
    public EngineNodeHandler(DataCache dataCache) {
        super(dataCache, EngineConfigurationModel.class);
    }

    @Override
    protected EngineConfigurationModel loadObject() {
        EngineConfigurationModel model = DependencyResolver.resolve(EngineConfigurationModel.class);
        model.initialize();
        return model;
    }

    @Override
    protected String saveObject(EngineConfigurationModel changedObject) {
        changedObject.save();
        return null;
    }
}
