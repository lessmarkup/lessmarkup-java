/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.security.models;

import com.lessmarkup.interfaces.system.MailTemplateModel;

public class GeneratedPasswordModel extends MailTemplateModel {
    private String login;
    private String password;
    private String siteName;
    private String siteLink;

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the siteName
     */
    public String getSiteName() {
        return siteName;
    }

    /**
     * @param siteName the siteName to set
     */
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    /**
     * @return the siteLink
     */
    public String getSiteLink() {
        return siteLink;
    }

    /**
     * @param siteLink the siteLink to set
     */
    public void setSiteLink(String siteLink) {
        this.siteLink = siteLink;
    }
}
