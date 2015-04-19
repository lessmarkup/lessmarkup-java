/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.system;

import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.interfaces.system.EngineConfiguration;
import com.thoughtworks.xstream.XStream;

import javax.servlet.ServletConfig;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class CustomizationFile {
    private List<CustomizationFileEntry> entries;

    public List<CustomizationFileEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<CustomizationFileEntry> entries) {
        this.entries = entries;
    }
}

class CustomizationFileEntry {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

class EngineConfigurationImpl implements EngineConfiguration {

    private final String CONFIGURATION_DIRECTORY = "/config";
    private final String CONFIGURATION_FILE = CONFIGURATION_DIRECTORY + "/engine.xml";

    private final ServletConfig servletConfig;
    private final Map<String, String> overrides = new HashMap<>();
    private boolean overridesInitialized = false;

    public EngineConfigurationImpl(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    private XStream getXmlStream() {
        XStream xStream = new XStream();
        xStream.alias("file", CustomizationFile.class);
        xStream.alias("entry", CustomizationFileEntry.class);
        return xStream;
    }

    private void loadOverrides() {
        if (!overridesInitialized) {
            overridesInitialized = true;

            String configurationPath = servletConfig.getServletContext().getRealPath("/") + CONFIGURATION_FILE;

            File file = new File(configurationPath);
            if (file.exists()) {
                try (Reader reader = new FileReader(configurationPath)) {
                    XStream stream = getXmlStream();
                    CustomizationFile entries = (CustomizationFile) stream.fromXML(reader);
                    if (entries != null && entries.getEntries() != null) {
                        for (CustomizationFileEntry entry : entries.getEntries()) {
                            if (entry.getKey() != null && entry.getValue() != null) {
                                overrides.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                } catch (IOException e) {
                    LoggingHelper.logException(getClass(), e);
                }
            }
        }
    }

    private void saveOverrides() {
        loadOverrides();

        CustomizationFile customizationFile = new CustomizationFile();
        List<CustomizationFileEntry> entries = new LinkedList<>();
        customizationFile.setEntries(entries);

        for (Map.Entry<String, String> source : overrides.entrySet()) {
            CustomizationFileEntry target = new CustomizationFileEntry();
            target.setKey(source.getKey());
            target.setValue(source.getValue());
            entries.add(target);
        }
        
        String rootPath = servletConfig.getServletContext().getRealPath("/");

        new File(rootPath + CONFIGURATION_DIRECTORY).mkdirs();

        String configurationPath = rootPath + CONFIGURATION_FILE;

        try (Writer writer = new FileWriter(configurationPath)) {
            XStream xStream = getXmlStream();
            xStream.toXML(customizationFile, writer);
        } catch (IOException e) {
            LoggingHelper.logException(getClass(), e);
        }
    }
    
    private String getString(String parameterName, String defaultValue) {
        String value = null;
        
        loadOverrides();
        value = overrides.get(parameterName);
        if (value == null) {
            value = servletConfig.getInitParameter(parameterName);
        }
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    private void setString(String parameterName, String value) {
        loadOverrides();
        overrides.put(parameterName, value);
        saveOverrides();
    }

    private boolean getBoolean(String parameterName, boolean defaultValue) {
        String value = getString(parameterName, null);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.equals("true") || !value.equals("false") && defaultValue;
    }

    private void setBoolean(String parameterName, boolean value) {
        setString(parameterName, Boolean.toString(value));
    }

    private int getInteger(String parameterName, int defaultValue) {
        String value = getString(parameterName, null);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    private void setInteger(String parameterName, int value) {
        setString(parameterName, Integer.toString(value));
    }

    @Override
    public boolean isSafeMode() {
        return getBoolean("safeMode", false);
    }

    @Override
    public void setSafeMode(boolean safeMode) {
        setBoolean("safeMode", safeMode);
    }

    @Override
    public String getFatalErrorsEmail() {
        return getString("fatalErrors", null);
    }

    @Override
    public void setFatalErrorsEmail(String email) {
        setString("fatalErrors", email);
    }

    @Override
    public boolean isSmtpConfigured() {
        String smtpServer = getSmtpServer();
        return smtpServer != null && smtpServer.length() > 0;
    }

    @Override
    public String getSmtpServer() {
        return getString("smtpServer", null);
    }

    @Override
    public void setSmtpServer(String server) {
        setString("smtpServer", server);
    }

    @Override
    public String getSmtpUsername() {
        return getString("smtpUsername", null);
    }

    @Override
    public void setSmtpUsername(String name) {
        setString("smtpUsername", name);
    }

    @Override
    public String getSmtpPassword() {
        return getString("smtpPassword", null);
    }

    @Override
    public void setSmtpPassword(String password) {
        setString("smtpPassword", password);
    }

    @Override
    public boolean isSmtpSsl() {
        return getBoolean("smtpSsl", false);
    }

    @Override
    public void setSmtpSsl(boolean ssl) {
        setBoolean("smtpSsl", ssl);
    }

    @Override
    public String getRecaptchaPublicKey() {
        return getString("recaptchaPublicKey", null);
    }

    @Override
    public void setRecaptchaPublicKey(String key) {
        setString("recaptchaPublicKey", key);
    }

    @Override
    public String getRecaptchaPrivateKey() {
        return getString("recaptchaPrivateKey", null);
    }

    @Override
    public void setRecaptchaPrivateKey(String key) {
        setString("recaptchaPrivateKey", key);
    }

    @Override
    public boolean isUseTestMail() {
        return getBoolean("useTestMail", false);
    }

    @Override
    public void setUseTestMail(boolean use) {
        setBoolean("useTestMail", use);
    }

    @Override
    public String getNoReplyEmail() {
        return getString("noReplyEmail", "no@reply.email");
    }

    @Override
    public void setNoReplyEmail(String email) {
        setString("noReplyEmail", email);
    }

    @Override
    public String getNoReplyName() {
        return getString("noReplyName", "NoReply");
    }

    @Override
    public void setNoReplyName(String name) {
        setString("noReplyName", name);
    }

    @Override
    public int getFailedAttemptsRememberMinutes() {
        return getInteger("failedAttemptsRememberMinutes", 15);
    }

    @Override
    public void setFailedAttemptsRememberMinutes(int minutes) {
        setInteger("failedAttemptsRememberMinutes", minutes);
    }

    @Override
    public int getMaximumFailedAttempts() {
        return getInteger("maximumFailedAttempts", 5);
    }

    @Override
    public void setMaximumFailedAttempts(int attempts) {
        setInteger("maximumFailedAttempts", attempts);
    }

    @Override
    public int getRecordsPerPage() {
        return getInteger("recordsPerPage", 10);
    }

    @Override
    public void setRecordsPerPage(int recordsPerPage) {
        setInteger("recordsPerPage", recordsPerPage);
    }

    @Override
    public String getAuthCookieName() {
        return getString("authCookieName", "LessMarkup_Auth");
    }

    @Override
    public void setAuthCookieName(String name) {
        setString("authCookieName", name);
    }

    @Override
    public int getAuthCookieTimeout() {
        return getInteger("authCookieTimeout", 15);
    }

    @Override
    public void setAuthCookieTimeout(int timeout) {
        setInteger("authCookieTimeout", timeout);
    }

    @Override
    public String getAuthCookiePath() {
        return getString("authCookiePath", "/");
    }

    @Override
    public void setAuthCookiePath(String path) {
        setString("authCookiePath", path);
    }

    @Override
    public boolean isAutoRefresh() {
        return getBoolean("autoRefresh", true);
    }

    @Override
    public void setAutoRefresh(boolean autoRefresh) {
        setBoolean("autoRefresh", autoRefresh);
    }

    @Override
    public String getNoAdminName() {
        return getString("noAdminName", "noadmin@noadmin.com");
    }

    @Override
    public void setNoAdminName(String noAdminName) {
        setString("noAdminName", noAdminName);
    }

    @Override
    public int getBackgroundJobInterval() {
        return getInteger("backgroundJobInterval", 10);
    }

    @Override
    public void setBackgroundJobInterval(int interval) {
        setInteger("backgroundJobInterval", interval);
    }

    @Override
    public String getAdminLoginPage() {
        return getString("adminLoginPage", "Login");
    }

    @Override
    public void setAdminLoginPage(String adminLoginPage) {
        setString("adminLoginPage", adminLoginPage);
    }

    @Override
    public String getAdminLoginAddress() {
        return getString("adminLoginAddress", null);
    }

    @Override
    public void setAdminLoginAddress(String adminLoginAddress) {
        setString("adminLoginAddress", adminLoginAddress);
    }

    @Override
    public boolean isMigrateDataLossAllowed() {
        return getBoolean("migrateDataLossAllowed", false);
    }

    @Override
    public void setMigrateDataLossAllowed(boolean migrateDataLossAllowed) {
        setBoolean("migrateDataLossAllowed", migrateDataLossAllowed);
    }

    @Override
    public boolean isCustomizationsDisabled() {
        return getBoolean("customizationsDisabled", false);
    }

    @Override
    public void setCustomizationsDisabled(boolean customizationsDisabled) {
        setBoolean("customizationsDisabled", customizationsDisabled);
    }

    @Override
    public String getConnectionString() {
        return getString("connectionString", null);
    }

    @Override
    public void setConnectionString(String connectionString) {
        setString("connectionString", connectionString);
    }

    @Override
    public String getModulesPath() {
        return getString("modulesPath", "modules");
    }

    @Override
    public void setModulesPath(String modulesPath) {
        setString("modulesPath", modulesPath);
    }

    @Override
    public String getSessionKey() {
        return getString("sessionKey", null);
    }

    @Override
    public void setSessionKey(String sessionKey) {
        setString("sessionKey", sessionKey);
    }
}
