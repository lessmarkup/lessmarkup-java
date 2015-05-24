package com.lessmarkup.userinterface.model.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.recordmodel.RecordModelCache;
import com.lessmarkup.interfaces.recordmodel.RecordModelDefinition;
import com.lessmarkup.interfaces.security.UserSecurity;
import com.lessmarkup.interfaces.system.SiteConfiguration;

public class RegisterModel extends RecordModel<RegisterModel> {

    private final DataCache dataCache;
    private final UserSecurity userSecurity;

    private String email;
    private String name;
    private boolean generatePassword;
    private String password;
    private boolean showUserAgreement;
    private String userAgreement;
    private boolean agree;

    @Inject
    public RegisterModel(DataCache dataCache, UserSecurity userSecurity) {
        this.dataCache = dataCache;
        this.userSecurity = userSecurity;
    }

    public JsonElement getRegisterObject() {
        SiteConfiguration siteProperties = dataCache.get(SiteConfiguration.class);

        if (!siteProperties.getHasUsers()) {
            throw new CommonException(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.CANNOT_REGISTER_NEW_USER));
        }

        userAgreement = siteProperties.getUserAgreement();
        showUserAgreement = !StringHelper.isNullOrWhitespace(siteProperties.getUserAgreement());

        RecordModelCache modelCache = dataCache.get(RecordModelCache.class);

        JsonObject ret = new JsonObject();
        ret.add("registerObject", JsonSerializer.serializePojoToTree(this));
        ret.addProperty("modelId", modelCache.getDefinition(RegisterModel.class).get().getId());

        return ret;
    }

    public JsonElement register() {
        SiteConfiguration siteProperties = dataCache.get(SiteConfiguration.class);

        if (!siteProperties.getHasUsers()) {
            throw new CommonException(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.CANNOT_REGISTER_NEW_USER));
        }

        RecordModelCache modelCache = dataCache.get(RecordModelCache.class);
        RecordModelDefinition definition = modelCache.getDefinition(RegisterModel.class).get();
        definition.validateInput(JsonSerializer.serializePojoToTree(this), true);

        userSecurity.createUser(name, password, email, false, false);

        boolean loggedIn = RequestContextHolder.getContext().getCurrentUser().loginWithPassword(email, password, false, false, true, null);

        JsonObject ret = new JsonObject();

        ret.addProperty("userName", name);
        ret.addProperty("showConfiguration", false);
        ret.addProperty("userLoggedIn", loggedIn);

        return ret;
    }

    @InputField(type = InputFieldType.EMAIL, textId = TextIds.EMAIL, required = true)
    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.USER_NAME, required = true)
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.GENERATE_PASSWORD)
    public void setGeneratePassword(boolean generatePassword) { this.generatePassword = generatePassword; }
    public boolean isGeneratePassword() { return generatePassword; }

    @InputField(type = InputFieldType.PASSWORD_REPEAT, textId = TextIds.PASSWORD, required = true, visibleCondition = "!generatePassword")
    public void setPassword(String password) { this.password = password; }
    public String getPassword() { return password; }

    public void setShowUserAgreement(boolean showUserAgreement) { this.showUserAgreement = showUserAgreement; }
    public boolean isShowUserAgreement() { return showUserAgreement; }

    @InputField(type = InputFieldType.RICH_TEXT, textId = TextIds.USER_AGREEMENT, visibleCondition = "showUserAgreement")
    public void setUserAgreement(String userAgreement) { this.userAgreement = userAgreement; }
    public String getUserAgreement() { return userAgreement; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.AGREE, required = true, visibleCondition = "showUserAgreement")
    public void setAgree(boolean agree) { this.agree = agree; }
    public boolean isAgree() { return agree; }
}
