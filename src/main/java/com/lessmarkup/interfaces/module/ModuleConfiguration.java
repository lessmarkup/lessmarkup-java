/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lessmarkup.interfaces.module;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.apache.commons.io.IOUtils;

public interface ModuleConfiguration {
    List<String> getElements();
    boolean isSystem();
    URL getUrl();
    String getModuleType();
    ClassLoader getClassLoader();
    ModuleInitializer getInitializer();
    InputStream getResourceAsStream(String path) throws IOException;
    default byte[] getResourceAsBytes(String path) throws IOException {
        try (InputStream stream = getResourceAsStream(path)) {
            return IOUtils.toByteArray(stream);
        }
    }
}
