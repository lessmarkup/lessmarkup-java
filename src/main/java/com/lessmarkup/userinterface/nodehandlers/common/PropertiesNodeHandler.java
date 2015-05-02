package com.lessmarkup.userinterface.nodehandlers.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lessmarkup.framework.helpers.*;
import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.structure.Property;

import java.net.URL;
import java.util.*;

public abstract class PropertiesNodeHandler extends AbstractNodeHandler {

    private final ModuleProvider moduleProvider;
    private final List<JsonObject> properties = new ArrayList<>();

    protected PropertiesNodeHandler(ModuleProvider moduleProvider) {
        this.moduleProvider = moduleProvider;
    }

    protected void addProperty(String name, InputFieldType type, String value) {
        JsonObject model = new JsonObject();
        model.addProperty("name", name);
        model.addProperty("type", type.toString());
        model.addProperty("value", value);
        properties.add(model);
    }

    @Override
    public String getViewType() {
        return "properties";
    }

    @Override
    public JsonObject getViewData() {

        URL moduleUrl = getClass().getProtectionDomain().getCodeSource().getLocation();

        Optional<ModuleConfiguration> moduleConfiguration = moduleProvider.getModules().stream().filter(m -> m.getUrl().equals(moduleUrl)).findFirst();

        JsonObject ret = new JsonObject();
        JsonArray propertiesArray = new JsonArray();
        
        for (PropertyDescriptor property : TypeHelper.getProperties(getClass())) {
            Property propertyAttribute = property.getAnnotation(Property.class);

            if (propertyAttribute == null) {
                continue;
            }

            Object value = property.getValue(this);

            if (value == null) {
                continue;
            }

            JsonObject model = new JsonObject();
            model.addProperty("name", LanguageHelper.getFullTextId(moduleConfiguration.get().getModuleType(), propertyAttribute.textId()));
            model.addProperty("type", propertyAttribute.type().toString());
            model.addProperty("value", value.toString());
            
            switch (propertyAttribute.type()) {
                case IMAGE:
                    long imageId = Long.parseLong(value.toString());
                    model.addProperty("value", ImageHelper.getImageUrl(imageId));
                    break;
            }

            propertiesArray.add(model);
        }

        for (JsonObject item : this.properties) {
            propertiesArray.add(item);
        }

        ret.add("properties", propertiesArray);

        return ret;
    }
}
