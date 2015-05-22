package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import com.lessmarkup.userinterface.model.user.LoginModel;
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler;

public class LoginNodeHandler extends DialogNodeHandler<LoginModel> {

    private final DataCache dataCache;

    @Inject
    public LoginNodeHandler(DataCache dataCache) {
        super(dataCache, LoginModel.class);
        this.dataCache = dataCache;
    }

    @Override
    protected LoginModel loadObject() {
        return null;
    }

    @Override
    protected String saveObject(LoginModel changedObject) {
        return null;
    }

    @Override
    public JsonObject getViewData() {
        SiteConfiguration siteConfiguration = dataCache.get(SiteConfiguration.class);
        String adminLoginPage = siteConfiguration.getAdminLoginPage();

        JsonObject ret = super.getViewData();
        ret.addProperty("administratorKey", StringHelper.isNullOrEmpty(adminLoginPage) ? Constants.NodePath.ADMIN_LOGIN_DEFAULT_PAGE : adminLoginPage);
        return ret;
    }

    @Override
    public String getViewType() {
        return "login";
    }

    @Override
    protected String getApplyCaption() {
        return TextIds.LOGIN;
    }
}
