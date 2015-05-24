package com.lessmarkup.userinterface.model.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.annotations.InputField;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.exceptions.UnauthorizedAccessException;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.system.SiteConfiguration;

import java.util.Objects;
import java.util.Random;

public class LoginModel extends RecordModel<LoginModel> {

    private String email;
    private String password;
    private boolean remember;

    private final DataCache dataCache;

    @Inject
    public LoginModel(DataCache dataCache) {
        super(TextIds.LOGIN);
        this.dataCache = dataCache;
    }

    public JsonObject handleStage1Request(JsonObject data) {
        try {
            Thread.sleep(new Random(System.currentTimeMillis()).nextInt(30));
        } catch (InterruptedException e) {
            throw new CommonException(e);
        }

        scala.Tuple2<String, String> loginHash = RequestContextHolder.getContext().getCurrentUser().getLoginHash(data.get("user").getAsString());

        JsonObject ret = new JsonObject();
        ret.addProperty("pass1", loginHash._1());
        ret.addProperty("pass2", loginHash._2());

        return ret;
    }
    
    public JsonObject handleStage2Request(JsonObject data) {
        String email = data.get("user").getAsString();
        String passwordHash = data.get("hash").getAsString();
        boolean savePassword = data.get("remember").getAsBoolean();

        String administratorKey = "";

        JsonElement temp = data.get("administratorKey");

        if (temp != null && !temp.isJsonNull()) {
            administratorKey = temp.getAsString();
        }

        SiteConfiguration siteConfiguration = dataCache.get(SiteConfiguration.class);
        String adminLoginPage = siteConfiguration.getAdminLoginPage();
        if (StringHelper.isNullOrEmpty(adminLoginPage)) {
            adminLoginPage = Constants.NodePathAdminLoginDefaultPage();
        }

        boolean allowAdministrator = Objects.equals(administratorKey, adminLoginPage);

        boolean allowUser = StringHelper.isNullOrWhitespace(administratorKey);

        if (!RequestContextHolder.getContext().getCurrentUser().loginWithPassword(email, "", savePassword, allowAdministrator, allowUser, passwordHash)) {
            throw new UnauthorizedAccessException(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.USER_NOT_FOUND));
        }

        JsonObject ret = new JsonObject();

        ret.addProperty("path", StringHelper.isNullOrWhitespace(adminLoginPage) ? "" : "/");

        return ret;
    }
    
    public JsonObject handleLogout() {
        RequestContextHolder.getContext().getCurrentUser().logout();
        return new JsonObject();
    }

    @InputField(type = InputFieldType.EMAIL, textId = TextIds.EMAIL, required = true, position = 1)
    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }

    @InputField(type = InputFieldType.PASSWORD, textId = TextIds.PASSWORD, required = true, position = 2)
    public void setPassword(String password) { this.password = password; }
    public String getPassword() { return password; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.REMEMBER_ME, position = 3)
    public void setRemember(boolean remember) { this.remember = remember; }
    public boolean isRemember() { return this.remember; }
}
