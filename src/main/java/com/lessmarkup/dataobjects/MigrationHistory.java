/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

import java.time.OffsetDateTime;

public class MigrationHistory extends AbstractDataObject {
    private String moduleType;
    private String uniqueId;
    private OffsetDateTime created;

    @RequiredField
    public String getModuleType() {
        return moduleType;
    }
    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    @RequiredField
    public String getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @RequiredField
    public OffsetDateTime getCreated() {
        return created;
    }
    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }
}
