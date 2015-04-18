/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.module;

import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleInitializer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class ModuleConfigurationImpl implements ModuleConfiguration {
    private final URL url;
    private final boolean system;
    private final String moduleType;
    private final List<String> elements;
    private final ClassLoader classLoader;
    private final ModuleInitializer moduleInitializer;
    
    public ModuleConfigurationImpl(URL url, boolean system, String moduleType, List<String> elements, ClassLoader classLoader, ModuleInitializer moduleInitializer) {
        this.url = url;
        this.system = system;
        this.moduleType = moduleType;
        this.elements = elements;
        this.classLoader = classLoader;
        this.moduleInitializer = moduleInitializer;
    }
    
    @Override
    public List<String> getElements() {
        return elements;
    }
    
    @Override
    public boolean isSystem() {
        return this.system;
    }
    
    @Override
    public URL getUrl() {
        return this.url;
    }
    
    @Override
    public String getModuleType() {
        return this.moduleType;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
    
    @Override
    public ModuleInitializer getInitializer() {
        return this.moduleInitializer;
    }

    @Override
    public InputStream getResourceAsStream(String path) throws IOException {
        return getClassLoader().getResourceAsStream(path);
    }
}
