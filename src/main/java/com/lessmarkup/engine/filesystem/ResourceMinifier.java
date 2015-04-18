/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.filesystem;

import com.lessmarkup.Constants;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.thoughtworks.xstream.XStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
class ResourceMinifier {
    
    private final ModuleProvider moduleProvider;
    private final List<ResourceReference> jsToMinify = new LinkedList<>();
    private final List<ResourceReference> cssToMinify = new LinkedList<>();
    private final boolean IS_DEBUG = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp");

    @Autowired
    public ResourceMinifier(ModuleProvider moduleProvider) {
        this.moduleProvider = moduleProvider;
    }
    
    void minify(Map<String, ResourceReference> resources, ResourceCacheImpl resourceCache) {
        initialize(resourceCache, resources);
        
        String content = "";
        if (!jsToMinify.isEmpty()) {
            content = minifyContent(jsToMinify, content, true, resourceCache);
        } 
       
        ResourceReference reference = new ResourceReference();
        reference.setBinary(content.getBytes());
        
        resources.put(Constants.Minify.JS_MINIFY, reference);
        
        content = "";
        if (!cssToMinify.isEmpty()) {
            content = minifyContent(cssToMinify, content, true, resourceCache);
        }
        
        reference = new ResourceReference();
        reference.setBinary(content.getBytes());
        
        resources.put(Constants.Minify.CSS_MINIFY, reference);
    }
    
    private String minifyContent(List<ResourceReference> toMinify, String initialContent, boolean minifyJs, ResourceCacheImpl resourceCache) {
        StringBuilder content = new StringBuilder();
        content.append(initialContent);
        
        Minify minify;
        
        if (IS_DEBUG) {
            minify = null;
        } else {
            minify = minifyJs ? new JsMinify() : new CssMinify();
        }

        for (ResourceReference resource : toMinify) {
            if (resource.getBinary() == null) {
                continue;
            }
            
            String source = resourceCache.parseText(resource);
            
            if (IS_DEBUG) {
                content.append("/************************\r\n");
                content.append("/\r\n");
                content.append("/ ").append(resource.getPath()).append("\r\n");
                content.append("/\r\n");
                content.append("/*************************/\r\n");
                content.append(source);
            } else {
                String target = resource.isMinified() ? source : minify.process(source);
                content.append(target);
            }
            
            resource.setBinary(new byte[0]);
        }

        return content.toString();
    }

    private void initialize(ResourceCacheImpl resourceCache, Map<String, ResourceReference> resources) {
        for (ModuleConfiguration module : moduleProvider.getModules()) {
            for (String element : module.getElements()) {
                if (!element.endsWith(".minify.xml")) {
                    continue;
                }
                
                String xml = resourceCache.readText(element);

                if (xml == null || xml.length() == 0) {
                    continue;
                }

                XStream xstream = new XStream();
                xstream.alias("file", XmlMinifyFile.class);
                xstream.alias("resource", XmlMinifyResource.class);
                xstream.useAttributeFor(XmlMinifyResource.class, "plain");
                xstream.useAttributeFor(XmlMinifyResource.class, "minified");

                XmlMinifyFile file = (XmlMinifyFile) xstream.fromXML(xml);

                if (file.getResources() == null || file.getResources().isEmpty()) {
                    continue;
                }

                for (XmlMinifyResource resource : file.getResources()) {
                    initializeResource(resource, resources, resourceCache);
                }
            }
        }
    }

    private void initializeResource(XmlMinifyResource resource, Map<String, ResourceReference> resources, ResourceCacheImpl resourceCache) {
        String minifyPath;
        boolean minified;
        if (IS_DEBUG) {
            minifyPath = resource.getPlain();
            minified = false;
            if (minifyPath == null || minifyPath.length() == 0) {
                minifyPath = resource.getMinified();
                minified = true;
            }
        } else {
            minifyPath = resource.getMinified();
            minified = true;
            if (minifyPath == null || minifyPath.length() == 0) {
                minifyPath = resource.getPlain();
                minified = false;
            }
        }
        
        if (minifyPath == null || minifyPath.length() == 0) {
            return;
        }
        
        final String minifyPath2 = minifyPath;
        final boolean minified2 = minified;

        for (Map.Entry<String, ResourceReference> entry : resources.entrySet()) {
            String path = entry.getKey();
            ResourceReference reference = entry.getValue();
            
            if (!path.startsWith(minifyPath2)) {
                continue;
            }
            int pos = path.lastIndexOf('.');
            if (pos <= 0) {
                continue;
            }
            String extension = path.substring(pos+1).toLowerCase();
            switch (extension) {
                case "js":
                    resourceCache.loadResource(reference.getPath());
                    reference.setMinified(minified2);
                    reference.setPath(minifyPath2);
                    jsToMinify.add(entry.getValue());
                    break;
                case "css":
                    resourceCache.loadResource(reference.getPath());
                    reference.setMinified(minified2);
                    reference.setPath(minifyPath2);
                    cssToMinify.add(entry.getValue());
                    break;
            }
        }
    }
}
