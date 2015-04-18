/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.filesystem;

import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.samskivert.mustache.Template;

class ResourceReference {
    private long recordId;
    private byte[] binary;
    private boolean minified;
    private String path;
    private ModuleConfiguration module;
    private Template template;

    long getRecordId() {
        return recordId;
    }

    void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    byte[] getBinary() {
        return binary;
    }

    void setBinary(byte[] binary) {
        this.binary = binary;
    }

    boolean isMinified() {
        return minified;
    }

    void setMinified(boolean minified) {
        this.minified = minified;
    }

    String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    ModuleConfiguration getModule() {
        return module;
    }

    void setModule(ModuleConfiguration module) {
        this.module = module;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }
}
