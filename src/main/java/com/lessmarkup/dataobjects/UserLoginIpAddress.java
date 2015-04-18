/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

import java.time.OffsetDateTime;

public class UserLoginIpAddress extends AbstractDataObject {
    private long userId;
    private String address;
    private OffsetDateTime created;

    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }

    @RequiredField
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    @RequiredField
    public OffsetDateTime getCreated() {
        return created;
    }
    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }
}
