/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.userinterface.model.global;

import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.system.EngineConfiguration;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class EmailConfigurationModel extends RecordModel<EmailConfigurationModel> {
    private String server;
    private String username;
    private String password;
    private boolean useSsl;
    private boolean useTestMail;
    private String noReplyMail;
    private String noReplyName;
    private final DataCache dataCache;

    @Autowired
    public EmailConfigurationModel(DataCache dataCache) {
        this.dataCache = dataCache;
    }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.SMTP_SERVER)
    public void setServer(String server) { this.server = server; }
    public String getServer() { return server; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.SMTP_USERNAME)
    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }

    @InputField(type = InputFieldType.PASSWORD, textId = TextIds.SMTP_PASSWORD)
    public void setPassword(String password) { this.password = password; }
    public String getPassword() { return password; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.SMTP_USE_SSL)
    public void setUseSsl(boolean useSsl) { this.useSsl = useSsl; }
    public boolean isUseSsl() { return useSsl; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.USE_TEST_MAIL)
    public void setUseTestMail(boolean useTestMail) { this.useTestMail = useTestMail; }
    public boolean isUseTestMail() { return useTestMail; }

    @InputField(type = InputFieldType.EMAIL, textId = TextIds.NO_REPLY_EMAIL)
    public void setNoReplyMail(String noReplyMail) { this.noReplyMail = noReplyMail; }
    public String getNoReplyMail() { return noReplyMail; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.NO_REPLY_NAME)
    public void setNoReplyName(String noReplyName) { this.noReplyName = noReplyName; }
    public String getNoReplyName() { return noReplyName; }
    
    public void initialize() {
        EngineConfiguration engineConfiguration = RequestContextHolder.getContext().getEngineConfiguration();
        server = engineConfiguration.getSmtpServer();
        username = engineConfiguration.getSmtpUsername();
        password = engineConfiguration.getSmtpPassword();
        useSsl = engineConfiguration.isSmtpSsl();
        useTestMail = engineConfiguration.isUseTestMail();
        noReplyMail = engineConfiguration.getNoReplyEmail();
        noReplyName = engineConfiguration.getNoReplyName();
    }
    
    public void save() {
        EngineConfiguration engineConfiguration = RequestContextHolder.getContext().getEngineConfiguration();
        engineConfiguration.setSmtpServer(server);
        engineConfiguration.setSmtpUsername(username);
        engineConfiguration.setSmtpPassword(password);
        engineConfiguration.setSmtpSsl(useSsl);
        engineConfiguration.setUseTestMail(useTestMail);
        engineConfiguration.setNoReplyEmail(noReplyMail);
        engineConfiguration.setNoReplyName(noReplyName);
    }
}
