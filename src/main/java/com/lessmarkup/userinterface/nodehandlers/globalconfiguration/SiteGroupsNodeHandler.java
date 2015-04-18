package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.lessmarkup.TextIds;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.userinterface.model.global.UserGroupModel;
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ConfigurationHandler(titleTextId = TextIds.GROUPS)
@Component
@Scope("prototype")
public class SiteGroupsNodeHandler extends RecordListNodeHandler<UserGroupModel> {

    @Autowired
    public SiteGroupsNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache) {
        super(domainModelProvider, dataCache, UserGroupModel.class);
    }
}
