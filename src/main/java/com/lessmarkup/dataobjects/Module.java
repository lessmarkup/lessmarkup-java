/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

public class Module extends AbstractDataObject {
    private String name;
    private String path;
    private boolean enabled;
    private boolean removed;
    private boolean system;
    private String moduleType;

    @RequiredField
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @RequiredField
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRemoved() {
        return removed;
    }
    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public boolean isSystem() {
        return system;
    }
    public void setSystem(boolean system) {
        this.system = system;
    }

    @RequiredField
    public String getModuleType() {
        return moduleType;
    }
    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }
}
