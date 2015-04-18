package com.lessmarkup.userinterface.nodehandlers.configuration;

import com.lessmarkup.TextIds;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.userinterface.model.configuration.UserPropertyModel;
import com.lessmarkup.userinterface.nodehandlers.common.RecordListLinkNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ConfigurationHandler(titleTextId = TextIds.USER_PROPERTIES)
@Component
@Scope("prototype")
public class UserPropertiesNodeHandler extends RecordListLinkNodeHandler<UserPropertyModel> {

    @Autowired
    public UserPropertiesNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache) {
        super(domainModelProvider, dataCache, UserPropertyModel.class);
    }
}
