package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.google.inject.Inject;
import com.lessmarkup.TextIds;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.userinterface.model.global.CustomizationModel;
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler;

@ConfigurationHandler(titleTextId = TextIds.CUSTOMIZE)
public class SiteCustomizeNodeHandler extends RecordListNodeHandler<CustomizationModel> {

    @Inject
    public SiteCustomizeNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache) {
        super(domainModelProvider, dataCache, CustomizationModel.class);
    }
}
