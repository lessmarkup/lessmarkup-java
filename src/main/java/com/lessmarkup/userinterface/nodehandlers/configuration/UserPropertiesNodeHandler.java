package com.lessmarkup.userinterface.nodehandlers.configuration;

import com.google.inject.Inject;
import com.lessmarkup.TextIds;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.userinterface.model.configuration.UserPropertyModel;
import com.lessmarkup.userinterface.nodehandlers.common.RecordListLinkNodeHandler;

@ConfigurationHandler(titleTextId = TextIds.USER_PROPERTIES)
public class UserPropertiesNodeHandler extends RecordListLinkNodeHandler<UserPropertyModel> {

    @Inject
    public UserPropertiesNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache) {
        super(domainModelProvider, dataCache, UserPropertyModel.class);
    }
}
