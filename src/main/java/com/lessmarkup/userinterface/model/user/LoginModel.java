package com.lessmarkup.userinterface.model.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.exceptions.UnauthorizedAccessException;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.structure.Tuple;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Random;

@Component
@Scope("prototype")
public class LoginModel extends RecordModel<LoginModel> {

    private String email;
    private String password;

    private final CurrentUser currentUser;
    private final DataCache dataCache;

    @Autowired
    public LoginModel(CurrentUser currentUser, DataCache dataCache) {
        this.dataCache = dataCache;
        this.currentUser = currentUser;
    }

    public JsonObject handleStage1Request(JsonObject data) {
        try {
            Thread.sleep(new Random(System.currentTimeMillis()).nextInt(30));
        } catch (InterruptedException e) {
            throw new CommonException(e);
        }

        Tuple<String, String> loginHash = null;

        try {
            loginHash = currentUser.getLoginHash(data.get("user").getAsString());
        } catch (NoSuchAlgorithmException | SQLException e) {
            throw new CommonException(e);
        }

        JsonObject ret = new JsonObject();
        ret.addProperty("pass1", loginHash.getValue1());
        ret.addProperty("pass2", loginHash.getValue2());

        return ret;
    }
    
    public JsonObject handleStage2Request(JsonObject data) {
        String email = data.get("user").getAsString();
        String passwordHash = data.get("hash").getAsString();
        boolean savePassword = data.get("remember").getAsBoolean();

        String administratorKey = "";

        JsonElement temp = data.get("administratorKey");

        if (temp != null) {
            administratorKey = temp.getAsString();
        }

        SiteConfiguration siteConfiguration = dataCache.get(SiteConfiguration.class);
        String adminLoginPage = siteConfiguration.getAdminLoginPage();

        boolean allowAdministrator = StringHelper.isNullOrWhitespace(adminLoginPage) || administratorKey == adminLoginPage;

        boolean allowUser = StringHelper.isNullOrWhitespace(adminLoginPage);

        if (!currentUser.loginWithPassword(email, "", savePassword, allowAdministrator, allowUser, passwordHash)) {
            throw new UnauthorizedAccessException(LanguageHelper.getText(Constants.ModuleType.MAIN, TextIds.USER_NOT_FOUND));
        }

        JsonObject ret = new JsonObject();

        ret.addProperty("userName", currentUser.getEmail());
        ret.addProperty("showConfiguration", currentUser.isAdministrator());
        ret.addProperty("path", StringHelper.isNullOrWhitespace(adminLoginPage) ? "" : "/");

        return ret;
    }
    
    public JsonObject handleLogout() {
        currentUser.logout();
        return new JsonObject();
    }

    @InputField(type = InputFieldType.EMAIL, textId = TextIds.EMAIL, required = true)
    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }

    @InputField(type = InputFieldType.PASSWORD, textId = TextIds.PASSWORD, required = true)
    public void setPassword(String password) { this.password = password; }
    public String getPassword() { return password; }
}
