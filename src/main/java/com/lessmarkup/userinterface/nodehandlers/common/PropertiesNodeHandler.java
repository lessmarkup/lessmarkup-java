package com.lessmarkup.userinterface.nodehandlers.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lessmarkup.framework.helpers.*;
import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.structure.Property;
import com.lessmarkup.userinterface.model.common.PropertyModel;

import java.net.URL;
import java.util.*;

public abstract class PropertiesNodeHandler extends AbstractNodeHandler {
    class PropertyDefinition {
        private String name;
        private InputFieldType type;
        private Object value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public InputFieldType getType() {
            return type;
        }

        public void setType(InputFieldType type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    private final ModuleProvider moduleProvider;
    private final List<PropertyDefinition> properties = new ArrayList<>();

    protected PropertiesNodeHandler(ModuleProvider moduleProvider) {
        this.moduleProvider = moduleProvider;
    }

    protected void addProperty(String name, InputFieldType type, Object value) {
        PropertyDefinition definition = new PropertyDefinition();
        definition.setName(name);
        definition.setType(type);
        definition.setValue(value);
        properties.add(definition);
    }

    @Override
    public String getViewType() {
        return "Properties";
    }

    @Override
    public JsonObject getViewData() {

        URL moduleUrl = getClass().getProtectionDomain().getCodeSource().getLocation();

        Optional<ModuleConfiguration> moduleConfiguration = moduleProvider.getModules().stream().filter(m -> m.getUrl().equals(moduleUrl)).findFirst();

        Collection<PropertyDescriptor> typeProperties = TypeHelper.getProperties(getClass());

        List<PropertyModel> properties = new ArrayList<>();

        for (PropertyDescriptor property : typeProperties) {
            Property propertyAttribute = property.getAnnotation(Property.class);

            if (propertyAttribute == null) {
                continue;
            }

            Object value = property.getValue(this);

            if (value == null) {
                continue;
            }

            PropertyModel model = new PropertyModel();
            model.setName(LanguageHelper.getText(moduleConfiguration.get().getModuleType(), propertyAttribute.textId()));
            model.setValue(value);
            model.setType(propertyAttribute.type());

            switch (model.getType()) {
                case IMAGE:
                    Class<?> valueType = property.getType();
                    if (valueType.equals(OptionalLong.class)) {
                        OptionalLong imageId = (OptionalLong) model.getValue();
                        model.setValue(ImageHelper.getImageUrl(imageId.getAsLong()));
                    }
                    else if (valueType.equals(long.class)) {
                        long imageId = (long) model.getValue();
                        model.setValue(ImageHelper.getImageUrl(imageId));
                    }
                    break;
            }

            properties.add(model);
        }

        for (PropertyDefinition item : this.properties) {
            PropertyModel model = new PropertyModel();
            model.setName(item.getName());
            model.setValue(item.getValue());
            model.setType(item.getType());
            properties.add(model);
        }

        JsonObject ret = new JsonObject();
        JsonArray propertiesArray = new JsonArray();

        for (PropertyModel model : properties) {
            JsonObject o = new JsonObject();
            o.addProperty("name", model.getName());
            o.addProperty("type", model.getType().toString());
            o.add("value", JsonSerializer.serializeToTree(model.getValue()));
            propertiesArray.add(o);
        }

        ret.add("properties", propertiesArray);

        return ret;
    }
}
