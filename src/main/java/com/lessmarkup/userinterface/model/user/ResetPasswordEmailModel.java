package com.lessmarkup.userinterface.model.user;

import com.lessmarkup.interfaces.system.MailTemplateModel;

public class ResetPasswordEmailModel extends MailTemplateModel {
    private String resetUrl;
    private String siteName;

    public String getResetUrl() {
        return resetUrl;
    }

    public void setResetUrl(String resetUrl) {
        this.resetUrl = resetUrl;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
}
