package com.lessmarkup.interfaces.system;

import java.util.OptionalLong;

public interface MailSender {
    <T extends MailTemplateModel> void sendMail(Class<T> type, String smtpServer, String smtpUser, String smtpPassword, boolean smtpSsl, String emailFrom, String emailTo, String viewPath, T parameters);
    <T extends MailTemplateModel> void sendMail(Class<T> type, String emailFrom, String emailTo, String viewPath, T parameters);
    <T extends MailTemplateModel> void sendMail(Class<T> type, OptionalLong userIdFrom, OptionalLong userIdTo, String userEmailTo, String viewPath, T parameters);
    void sendPlainEmail(String emailTo, String subject, String message);
}
