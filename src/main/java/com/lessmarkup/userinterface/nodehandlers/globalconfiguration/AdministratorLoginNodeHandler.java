package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.google.gson.JsonObject;
import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AdministratorLoginNodeHandler extends AbstractNodeHandler {

    private final DataCache dataCache;

    @Autowired
    public AdministratorLoginNodeHandler(DataCache dataCache) {
        this.dataCache = dataCache;
    }

    @Override
    public JsonObject getViewData() {
        SiteConfiguration siteConfiguration = dataCache.get(SiteConfiguration.class);
        String adminLoginPage = siteConfiguration.getAdminLoginPage();

        JsonObject ret = new JsonObject();
        ret.addProperty("administratorKey", adminLoginPage);
        return ret;
    }
}
