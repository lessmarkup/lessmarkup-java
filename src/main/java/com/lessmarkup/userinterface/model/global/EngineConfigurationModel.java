/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.userinterface.model.global;

import com.google.inject.Inject;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.system.EngineConfiguration;

public class EngineConfigurationModel extends RecordModel<EngineConfigurationModel> {
    private boolean safeMode;
    private String connectionString;
    private String fatalErrorsEmail;
    private String recaptchaPublicKey;
    private String recaptchaPrivateKey;
    private int recordsPerPage;
    private String authCookieName;
    private int authCookieTimeout;
    private String authCookiePath;
    private boolean autoRefresh;
    private String noAdminName;
    private String adminLoginPage;
    private String adminLoginAddress;
    private boolean migrateDataLossAllowed;
    private boolean disableCustomizations;

    private final DataCache dataCache;

    @Inject
    public EngineConfigurationModel(DataCache dataCache) {
        this.dataCache = dataCache;
    }

    public void initialize() {
        EngineConfiguration engineConfiguration = RequestContextHolder.getContext().getEngineConfiguration();
        setSafeMode(engineConfiguration.isSafeMode());
        setConnectionString(engineConfiguration.getConnectionString());
        setFatalErrorsEmail(engineConfiguration.getFatalErrorsEmail());
        setRecaptchaPublicKey(engineConfiguration.getRecaptchaPublicKey());
        setRecaptchaPrivateKey(engineConfiguration.getRecaptchaPrivateKey());
        setRecordsPerPage(engineConfiguration.getRecordsPerPage());
        setAuthCookieName(engineConfiguration.getAuthCookieName());
        setAuthCookieTimeout(engineConfiguration.getAuthCookieTimeout());
        setAuthCookiePath(engineConfiguration.getAuthCookiePath());
        setAutoRefresh(engineConfiguration.isAutoRefresh());
        setNoAdminName(engineConfiguration.getNoAdminName());
        setAdminLoginPage(engineConfiguration.getAdminLoginPage());
        setAdminLoginAddress(engineConfiguration.getAdminLoginAddress());
        setMigrateDataLossAllowed(engineConfiguration.isMigrateDataLossAllowed());
        setDisableCustomizations(engineConfiguration.isCustomizationsDisabled());
    }

    public void save() {
        EngineConfiguration engineConfiguration = RequestContextHolder.getContext().getEngineConfiguration();
        engineConfiguration.setSafeMode(isSafeMode());
        engineConfiguration.setConnectionString(getConnectionString());
        engineConfiguration.setFatalErrorsEmail(getFatalErrorsEmail());
        engineConfiguration.setRecaptchaPublicKey(getRecaptchaPublicKey());
        engineConfiguration.setRecaptchaPrivateKey(getRecaptchaPrivateKey());
        engineConfiguration.setRecordsPerPage(getRecordsPerPage());
        engineConfiguration.setAuthCookieName(getAuthCookieName());
        engineConfiguration.setAuthCookieTimeout(getAuthCookieTimeout());
        engineConfiguration.setAuthCookiePath(getAuthCookiePath());
        engineConfiguration.setAutoRefresh(isAutoRefresh());
        engineConfiguration.setNoAdminName(getNoAdminName());
        engineConfiguration.setAdminLoginPage(getAdminLoginPage());
        engineConfiguration.setAdminLoginAddress(getAdminLoginAddress());
        engineConfiguration.setMigrateDataLossAllowed(isMigrateDataLossAllowed());
        engineConfiguration.setCustomizationsDisabled(isDisableCustomizations());

        dataCache.reset();
    }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.SAFE_MODE)
    public void setSafeMode(boolean safeMode) { this.safeMode = safeMode; }
    public boolean isSafeMode() { return safeMode; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.CONNECTION_STRING, required = true)
    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }
    public String getConnectionString() { return connectionString; }

    @InputField(type = InputFieldType.EMAIL, textId = TextIds.FATAL_ERRORS_EMAIL)
    public void setFatalErrorsEmail(String fatalErrorsEmail) { this.fatalErrorsEmail = fatalErrorsEmail; }
    public String getFatalErrorsEmail() { return fatalErrorsEmail; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.RECAPTCHA_PUBLIC_KEY)
    public void setRecaptchaPublicKey(String recaptchaPublicKey) { this.recaptchaPublicKey = recaptchaPublicKey; }
    public String getRecaptchaPublicKey() { return recaptchaPublicKey; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.RECAPTCHA_PRIVATE_KEY)
    public void setRecaptchaPrivateKey(String recaptchaPrivateKey) { this.recaptchaPrivateKey = recaptchaPrivateKey; }
    public String getRecaptchaPrivateKey() { return recaptchaPrivateKey; }

    @InputField(type = InputFieldType.NUMBER, textId = TextIds.RECORDS_PER_PAGE)
    public void setRecordsPerPage(int recordsPerPage) { this.recordsPerPage = recordsPerPage; }
    public int getRecordsPerPage() { return recordsPerPage; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.AUTH_COOKIE_NAME)
    public void setAuthCookieName(String authCookieName) { this.authCookieName = authCookieName; }
    public String getAuthCookieName() { return authCookieName; }

    @InputField(type = InputFieldType.NUMBER, textId = TextIds.AUTH_COOKIE_TIMEOUT)
    public void setAuthCookieTimeout(int authCookieTimeout) { this.authCookieTimeout = authCookieTimeout; }
    public int getAuthCookieTimeout() { return authCookieTimeout; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.AUTH_COOKIE_PATH)
    public void setAuthCookiePath(String authCookiePath) { this.authCookiePath = authCookiePath; }
    public String getAuthCookiePath() { return authCookiePath; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.AUTO_REFRESH)
    public void setAutoRefresh(boolean autoRefresh) { this.autoRefresh = autoRefresh; }
    public boolean isAutoRefresh() { return autoRefresh; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.NO_ADMIN_NAME)
    public void setNoAdminName(String noAdminName) { this.noAdminName = noAdminName; }
    public String getNoAdminName() { return noAdminName; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.ADMIN_LOGIN_PAGE)
    public void setAdminLoginPage(String adminLoginPage) { this.adminLoginPage = adminLoginPage; }
    public String getAdminLoginPage() { return adminLoginPage; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.ADMIN_LOGIN_ADDRESS)
    public void setAdminLoginAddress(String adminLoginAddress) { this.adminLoginAddress = adminLoginAddress; }
    public String getAdminLoginAddress() { return adminLoginAddress; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.MIGRATE_DATA_LOSS_ALLOWED)
    public void setMigrateDataLossAllowed(boolean migrateDataLossAllowed) { this.migrateDataLossAllowed = migrateDataLossAllowed; }
    public boolean isMigrateDataLossAllowed() { return migrateDataLossAllowed; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.DISABLE_CUSTOMIZATIONS)
    public void setDisableCustomizations(boolean disableCustomizations) { this.disableCustomizations = disableCustomizations; }
    public boolean isDisableCustomizations() { return disableCustomizations; }
}
