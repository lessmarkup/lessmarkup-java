/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.mail;

import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.dataobjects.TestMail;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.module.Implements;
import com.lessmarkup.interfaces.system.EngineConfiguration;
import com.lessmarkup.interfaces.system.MailSender;
import com.lessmarkup.interfaces.system.MailTemplateModel;
import com.lessmarkup.interfaces.system.MailTemplateProvider;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.OptionalLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SimpleSMTPHeader;

@Implements(MailSender.class)
public class MailSenderImpl implements MailSender {

    private final DomainModelProvider domainModelProvider;
    private final MailTemplateProvider mailTemplateProvider;
    private final DataCache dataCache;

    @Inject
    public MailSenderImpl(DomainModelProvider domainModelProvider, MailTemplateProvider mailTemplateProvider, DataCache dataCache) {
        this.domainModelProvider = domainModelProvider;
        this.mailTemplateProvider = mailTemplateProvider;
        this.dataCache = dataCache;
    }
    
    private String getNoReplyEmail() {
        SiteConfiguration siteConfiguration = dataCache.get(SiteConfiguration.class);
        String ret = siteConfiguration.getNoReplyEmail();
        if (ret == null || ret.length() == 0) {
            ret = RequestContextHolder.getContext().getEngineConfiguration().getNoReplyEmail();
            if (ret == null || ret.length() == 0) {
                ret = "no@reply.email";
            }
        }
        return ret;
    }
    
    private String getNoReplyName() {
        SiteConfiguration siteConfiguration = dataCache.get(SiteConfiguration.class);
        String ret = siteConfiguration.getNoReplyName();
        if (ret == null || ret.length() == 0) {
            ret = RequestContextHolder.getContext().getEngineConfiguration().getNoReplyName();
        }
        return ret;
    }
    
    private String composeAddress(String email, String name) {
        return String.format("\"%s\" <%s>", name, email);
    }
    
    @Override
    public <T extends MailTemplateModel> void sendMail(Class<T> type, String smtpServer, String smtpUser, String smtpPassword, boolean smtpSsl, String emailFrom, String emailTo, String viewPath, T parameters) {
        String body;
        String subject;
        parameters.setUserEmail(emailTo);
        body = mailTemplateProvider.executeTemplate(type, viewPath, parameters);
        subject = parameters.getSubject();
        sendMail("", emailFrom, parameters.getUserName(), emailTo, subject, body, viewPath, smtpServer, smtpUser, smtpPassword, smtpSsl);
    }

    @Override
    public <T extends MailTemplateModel> void sendMail(Class<T> type, String emailFrom, String emailTo, String viewPath, T parameters) {
        EngineConfiguration engineConfiguration = RequestContextHolder.getContext().getEngineConfiguration();
        sendMail(type, engineConfiguration.getSmtpServer(), engineConfiguration.getSmtpUsername(), engineConfiguration.getSmtpPassword(), 
                engineConfiguration.isSmtpSsl(), emailFrom, emailTo, viewPath, parameters);
    }

    @Override
    public <T extends MailTemplateModel> void sendMail(Class<T> type, OptionalLong userIdFrom, OptionalLong userIdTo, String userEmailTo, String viewPath, T parameters) {
        try {
            User userFrom = null;
            User userTo = null;
            try (DomainModel domainModel = domainModelProvider.create()) {
                if (userIdTo.isPresent()) {
                    userTo = domainModel.query().from(User.class).where(Constants.Data.ID_PROPERTY_NAME + " = $", userIdTo.getAsLong()).first(User.class);
                }
                if (userIdFrom.isPresent()) {
                    userFrom = domainModel.query().from(User.class).where(Constants.Data.ID_PROPERTY_NAME + " = $", userIdFrom.getAsLong()).first(User.class);
                }
            }
            
            String fromName;
            String fromEmail;
            
            if (userFrom != null) {
                fromEmail = userFrom.isShowEmail() ? userFrom.getEmail() : getNoReplyEmail();
                fromName = userFrom.getName();
            } else {
                fromEmail = getNoReplyEmail();
                fromName = getNoReplyName();
            }
            
            if (userTo != null) {
                parameters.setUserEmail(userTo.getEmail());
                parameters.setUserName(userTo.getName());
            } else if (userEmailTo != null && userEmailTo.length() > 0) {
                parameters.setUserEmail(userEmailTo);
            } else {
                throw new IllegalArgumentException();
            }
            
            String body = mailTemplateProvider.executeTemplate(type, viewPath, parameters);
            
            String subject = parameters.getSubject();
            
            if (fromEmail == null) {
                throw new IllegalArgumentException();
            }
            
            EngineConfiguration engineConfiguration = RequestContextHolder.getContext().getEngineConfiguration();
            
            sendMail(fromName, fromEmail, parameters.getUserName(), parameters.getUserEmail(), subject, body, viewPath, engineConfiguration.getSmtpServer(),
                    engineConfiguration.getSmtpUsername(), engineConfiguration.getSmtpPassword(), engineConfiguration.isSmtpSsl());
            
        } catch (Exception e) {
            Logger.getLogger(MailSenderImpl.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void sendPlainEmail(String emailTo, String subject, String message) {
        
    }
    
    private void sendMail(String fromName, String fromAddress, String toName, String toAddress, String subject, String body, String viewPath, String server, String username, String password, boolean useSsl) {
        if (RequestContextHolder.getContext().getEngineConfiguration().isUseTestMail()) {
            try (DomainModel domainModel = domainModelProvider.create()) {
                TestMail testMail = new TestMail();
                testMail.setBody(body);
                testMail.setFrom(composeAddress(fromAddress, fromName));
                testMail.setTemplate(viewPath);
                testMail.setSent(OffsetDateTime.now());
                testMail.setSubject(subject);
                testMail.setTo(composeAddress(toAddress, toName));
                testMail.setViews(0);
                domainModel.create(testMail);
            } catch (Exception ex) {
                Logger.getLogger(MailSenderImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        
        if (server == null || server.length() == 0) {
            throw new IllegalArgumentException();
        }
        
        AuthenticatingSMTPClient client;
        
        try {
            if (useSsl) {
                client = new AuthenticatingSMTPClient("TLS", true);
            } else {
                client = new AuthenticatingSMTPClient();
            }

            client.connect(server);

            client.ehlo("localhost");
            client.auth(AuthenticatingSMTPClient.AUTH_METHOD.LOGIN, username, password);
            client.setSender(composeAddress(fromAddress, fromName));
            client.addRecipient(composeAddress(toAddress, toName));

            try (Writer writer = client.sendMessageData()) {
                SimpleSMTPHeader header = new SimpleSMTPHeader(fromAddress, toAddress, subject);
                writer.write(header.toString());
                writer.write(body);
            }

            client.completePendingCommand();
            client.logout();
            client.disconnect();
        } catch (Exception e) {
            Logger.getLogger(MailSenderImpl.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
