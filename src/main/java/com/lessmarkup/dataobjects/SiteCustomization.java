/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

import java.time.OffsetDateTime;

public class SiteCustomization extends AbstractDataObject {
    @RequiredField
    private String path;
    private boolean isBinary;
    @RequiredField
    private byte[] body;
    @RequiredField
    private OffsetDateTime created;
    private OffsetDateTime updated;
    private boolean append;

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the isBinary
     */
    public boolean isIsBinary() {
        return isBinary;
    }

    /**
     * @param isBinary the isBinary to set
     */
    public void setIsBinary(boolean isBinary) {
        this.isBinary = isBinary;
    }

    /**
     * @return the body
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(byte[] body) {
        this.body = body;
    }

    /**
     * @return the created
     */
    public OffsetDateTime getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    /**
     * @return the updated
     */
    public OffsetDateTime getUpdated() {
        return updated;
    }

    /**
     * @param updated the updated to set
     */
    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    /**
     * @return the append
     */
    public boolean isAppend() {
        return append;
    }

    /**
     * @param append the append to set
     */
    public void setAppend(boolean append) {
        this.append = append;
    }
}
