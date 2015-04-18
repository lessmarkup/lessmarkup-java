/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lessmarkup.framework.nodehandlers;

import com.google.gson.JsonObject;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.Tuple;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

/**
 *
 * @author User
 */
public abstract class AbstractNodeHandler implements NodeHandler {
    private OptionalLong objectId;
    private Object settings;
    private NodeAccessType accessType;
    private final List<String> scripts = new ArrayList<>();
    private final List<String> stylesheets = new ArrayList<>();
    private String path;
    private String fullPath;

    protected NodeHandler createChildHandler(Class<? extends NodeHandler> handlerType) {
        return DependencyResolver.resolve(handlerType);
    }
    
    @Override
    public OptionalLong getObjectId() {
        return this.objectId;
    }
    
    public String getPath() {
        return this.path;
    }
    
    @Override
    public String getViewType() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    public String getTemplateId() {
        return getViewType().toLowerCase();
    }
    
    public String getFullPath() {
        return this.fullPath;
    }
    
    @Override
    public NodeAccessType getAccessType() {
        return this.accessType;
    }
    
    public boolean hasManageAccess() {
        return this.accessType == NodeAccessType.MANAGE;
    }
    
    public boolean hasWriteAccess() {
        return hasManageAccess() || this.accessType == NodeAccessType.WRITE;
    }
    
    protected <T> T getSettings(Class<T> type) {
        return (T) this.settings;
    }
    
    protected void addScript(String script) {
        this.scripts.add(script);
    }
    
    protected void addStylesheet(String stylesheet) {
        this.stylesheets.add(stylesheet);
    }
    
    protected Object initialize() {
        return null;
    }

    public boolean trySubmitResponse(String path) throws IOException { return false; }

    @Override
    public Class<?> getSettingsModel() {
        return null;
    }

    @Override
    public Object initialize(OptionalLong objectId, JsonObject settings, String path, String fullPath, NodeAccessType accessType) {
        this.objectId = objectId;
        this.path = path;
        this.settings = settings;
        this.accessType = accessType;
        this.fullPath = fullPath;
        return initialize();
    }
    
    @Override
    public List<String> getScripts() {
        return this.scripts;
    }

    @Override
    public List<String> getStylesheets() {
        return this.stylesheets;
    }

    @Override
    public Tuple<Object, Method> getActionHandler(String name, JsonObject data) {
        name = StringHelper.toJsonCase(name);
        for (Method method : getClass().getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                return null;
            }

            if (name.equals(method.getName())) {
                return new Tuple<>(this, method);
            }
        }
        return null;
    }

    @Override
    public boolean processUpdates(OptionalLong fromVersion, long toVersion, JsonObject returnValues, DomainModel domainModel, JsonObject arguments) {
        return false;
    }
}
