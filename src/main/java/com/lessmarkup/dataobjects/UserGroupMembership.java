/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;

public class UserGroupMembership extends AbstractDataObject {
    private long userId;
    private long userGroupId;

    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUserGroupId() {
        return userGroupId;
    }
    public void setUserGroupId(long userGroupId) {
        this.userGroupId = userGroupId;
    }
}
