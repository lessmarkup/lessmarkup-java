package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.google.inject.Inject;
import com.lessmarkup.TextIds;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.userinterface.model.global.UserGroupModel;
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler;

@ConfigurationHandler(titleTextId = TextIds.GROUPS)
public class SiteGroupsNodeHandler extends RecordListNodeHandler<UserGroupModel> {

    @Inject
    public SiteGroupsNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache) {
        super(domainModelProvider, dataCache, UserGroupModel.class);
    }
}
