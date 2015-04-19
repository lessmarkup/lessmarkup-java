package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.userinterface.model.global.EmailConfigurationModel;
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@ConfigurationHandler(titleTextId = TextIds.EMAIL_CONFIGURATION)
public class EmailNodeHandler extends DialogNodeHandler<EmailConfigurationModel> {

    @Autowired
    public EmailNodeHandler(DataCache dataCache) {
        super(dataCache, EmailConfigurationModel.class);
    }

    @Override
    protected EmailConfigurationModel loadObject() {
        EmailConfigurationModel model = DependencyResolver.resolve(EmailConfigurationModel.class);
        model.initialize();
        return model;
    }

    @Override
    protected String saveObject(EmailConfigurationModel changedObject) {
        changedObject.save();
        return null;
    }
}
