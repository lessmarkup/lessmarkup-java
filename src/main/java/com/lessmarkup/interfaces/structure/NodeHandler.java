package com.lessmarkup.interfaces.structure;

import com.google.gson.JsonObject;
import com.lessmarkup.interfaces.data.DomainModel;

import java.lang.reflect.Method;
import java.util.List;
import java.util.OptionalLong;

public interface NodeHandler {
    Object initialize(OptionalLong objectId, JsonObject settings, String path, String fullPath, NodeAccessType accessType);
    OptionalLong getObjectId();
    default JsonObject getViewData() { return null; }
    default boolean hasChildren() { return false; }
    default boolean isStatic() { return false; }
    default ChildHandlerSettings getChildHandler(String path) { return null; }
    List<String> getStylesheets();
    String getTemplateId();
    String getViewType();
    List<String> getScripts();
    NodeAccessType getAccessType();
    Tuple<Object, Method> getActionHandler(String name, JsonObject data);
    Class<?> getSettingsModel();
    boolean processUpdates(OptionalLong fromVersion, long toVersion, JsonObject returnValues, DomainModel domainModel, JsonObject arguments);
}
