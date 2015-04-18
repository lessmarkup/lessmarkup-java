/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.security.models;

import com.lessmarkup.interfaces.system.MailTemplateModel;

public class UserConfirmationMailTemplateModel extends MailTemplateModel {
    String link;
    
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}
