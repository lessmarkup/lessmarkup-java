package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.userinterface.model.structure.SitePropertiesModel;
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@ConfigurationHandler(titleTextId = TextIds.SITE_PROPERTIES)
public class SitePropertiesNodeHandler extends DialogNodeHandler<SitePropertiesModel> {

    @Autowired
    public SitePropertiesNodeHandler(DataCache dataCache) {
        super(dataCache, SitePropertiesModel.class);
    }

    @Override
    protected SitePropertiesModel loadObject() {
        SitePropertiesModel ret = DependencyResolver.resolve(SitePropertiesModel.class);
        ret.initialize(null);
        return ret;
    }

    @Override
    protected String saveObject(SitePropertiesModel changedObject) {
        changedObject.save();
        return null;
    }
}
