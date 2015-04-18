/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.filesystem;

import java.util.ArrayList;
import java.util.List;

class CacheItem {
    private final List<String> textParts = new ArrayList<>();
    private final List<Directive> directives = new ArrayList<>();
    private String path;
    private String moduleType;
    
    List<String> getTextParts() { return textParts; }
    List<Directive> getDirectives() { return directives; }
    String getPath() { return path; }
    void setPath(String path) { this.path = path; }
    String getModuleType() { return moduleType; }
    void setModuleType(String moduleType) { this.moduleType = moduleType; }
}
