/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.filesystem;

import com.google.inject.Inject;
import com.lessmarkup.dataobjects.SiteCustomization;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.AbstractCacheHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.module.Implements;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.system.ResourceCache;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.logging.Level;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

@Implements(ResourceCache.class)
class ResourceCacheImpl extends AbstractCacheHandler implements ResourceCache {

    private final ModuleProvider moduleProvider;
    private final DomainModelProvider domainModelProvider;
    private final DataCache dataCache;
    private final Map<String, ResourceReference> resources = new HashMap<>();
    private Mustache.Compiler compiler;

    @Inject
    public ResourceCacheImpl(ModuleProvider moduleProvider, DomainModelProvider domainModelProvider, DataCache dataCache) {
        super(null);
        this.moduleProvider = moduleProvider;
        this.domainModelProvider = domainModelProvider;
        this.dataCache = dataCache;
    }

    private static boolean isExtensionSupported(String extension) {
        switch (extension) {
            case "jpg":
            case "gif":
            case "png":
            case "html":
            case "xml":
            case "js":
            case "css":
            case "eot":
            case "svg":
            case "ttf":
            case "woff":
            case "woff2":
            case "map":
            case "ts":
                return true;
        }

        return false;
    }

    private static String extractExtension(String path) {
        int pos = path.lastIndexOf('.');
        if (pos <= 0) {
            return null;
        }

        return path.substring(pos+1).toLowerCase();
    }

    @Override
    public void initialize(OptionalLong objectId) {

        for (ModuleConfiguration module : moduleProvider.getModules()){
            for (String element : module.getElements()) {

                String extension = extractExtension(element);

                if (extension != null && isExtensionSupported(extension)) {
                    addResource(module, element, extension);
                }
            }
        }
        
        loadDatabaseResources();
        
        this.compiler = TemplateContext.createCompiler(this);
    }
    
    private void loadDatabaseResources() {
        if (RequestContextHolder.getContext().getEngineConfiguration().isCustomizationsDisabled()) {
            return;
        }
        
        try (DomainModel domainModel = domainModelProvider.create()) {
            for (SiteCustomization record : domainModel.query().from(SiteCustomization.class).toList(SiteCustomization.class)) {
                String recordPath = record.getPath().toLowerCase();
                
                ResourceReference reference;
                
                if (record.isAppend()) {
                    reference = resources.get(recordPath);
                    if (reference != null) {
                        byte[] binary = new byte[reference.getBinary().length + record.getBody().length];
                        System.arraycopy(reference.getBinary(), 0, binary, 0, reference.getBinary().length);
                        System.arraycopy(record.getBody(), 0, binary, reference.getBinary().length, record.getBody().length);
                        reference.setBinary(binary);
                        continue;
                    }
                }
                
                reference = new ResourceReference();
                reference.setRecordId(record.getId());
                reference.setBinary(record.getBody());
                
                resources.put(recordPath, reference);
            }
        }
    }
    
    private void addResource(ModuleConfiguration module, String path, String extension) {
        ResourceReference reference = new ResourceReference();
        reference.setModule(module);
        reference.setPath(path);
        reference.setExtension(extension);
        resources.put(path, reference);
    }
    
    @Override
    public boolean resourceExists(String path) {
        return resources.containsKey(path);
    }
    
    ResourceReference loadResource(String path) {
        ResourceReference resource = resources.get(path);
        if (resource == null) {
            return null;
        }
        
        if (resource.getBinary() != null) { 
            return resource;
        }
        
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized(resource) {
            try {
                resource.setBinary(resource.getModule().getResourceAsBytes(path));
            } catch (IOException e) {
                LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, e);
            }

            if (resource.getBinary() != null) {
                if ("html".equals(resource.getExtension())) {
                    String body = StringHelper.binaryToString(resource.getBinary());
                    if (body.contains("[[")) {
                        Template template = compiler.compile(body);
                        resource.setTemplate(template);
                    }
                }
            }
        }
        return resource;
    }
    
    @Override
    public byte[] readBytes(String path) {
        ResourceReference resource = loadResource(path);
        if (resource == null || resource.getBinary() == null) {
            return null;
        }
        return resource.getBinary();
    }

    @Override
    public String readText(String path) {
        ResourceReference resource = loadResource(path);
        if (resource == null || resource.getBinary() == null) {
            return null;
        }
        return StringHelper.binaryToString(resource.getBinary());
    }
    
    String parseText(ResourceReference reference) {
        if (reference.getTemplate() == null) {
            return StringHelper.binaryToString(reference.getBinary());
        }
        TemplateContext context = new TemplateContext(dataCache);
        return reference.getTemplate().execute(context);
    }

    @Override
    public String parseText(String path) {
        ResourceReference reference = loadResource(path);
        if (reference == null || reference.getBinary() == null) {
            return null;
        }
        return parseText(reference);
    }
}
