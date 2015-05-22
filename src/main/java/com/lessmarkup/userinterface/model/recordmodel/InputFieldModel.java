package com.lessmarkup.userinterface.model.recordmodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.interfaces.recordmodel.InputFieldDefinition;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordModelDefinition;
import com.lessmarkup.interfaces.recordmodel.SelectValueModel;

import java.util.ArrayList;
import java.util.List;

public class InputFieldModel {

    private final List<SelectValueModel> selectValues = new ArrayList<>();
    
    private final String text;
    private final InputFieldType type;
    private final boolean readOnly;
    private final String id;
    private final boolean required;
    private final double width;
    private final int minWidth;
    private final int maxWidth;
    private final int position;
    private final String readOnlyCondition;
    private final String visibleCondition;
    private final String property;
    private final String helpText;
    private final String defaultValue;
    private final Class propertyType;
    
    public InputFieldModel(InputFieldDefinition source, RecordModelDefinition definition) {
        this.id = source.getId();
        this.readOnly = source.isReadOnly();
        this.readOnlyCondition = source.getReadOnlyCondition();
        this.required = source.isRequired();
        this.text = LanguageHelper.getFullTextId(definition.getModuleType(), source.getTextId());
        this.type = source.getType();
        this.visibleCondition = source.getVisibleCondition();
        this.width = source.getWidth();
        this.minWidth = source.getMinWidth();
        this.maxWidth = source.getMaxWidth();
        this.position = source.getPosition();
        this.property = StringHelper.toJsonCase(source.getProperty().getName());
        this.propertyType = source.getProperty().getType();
        this.defaultValue = source.getDefaultValue();
        this.helpText = null;
    }
    
    public List<SelectValueModel> getSelectValues() { return selectValues; }

    public InputFieldType getType() {
        return this.type;
    }
    
    public JsonElement toJson() {
        JsonObject ret = new JsonObject();
        
        ret.addProperty("text", this.text);
        ret.addProperty("type", this.type.toString());
        ret.addProperty("readOnly", this.readOnly);
        ret.addProperty("id", StringHelper.toJsonCase(this.id));
        ret.addProperty("required", this.required);
        ret.addProperty("width", this.width);
        ret.addProperty("minWidth", this.minWidth);
        ret.addProperty("maxWidth", this.maxWidth);
        ret.addProperty("position", this.position);
        ret.addProperty("readOnlyCondition", this.readOnlyCondition);
        ret.addProperty("visibleCondition", this.visibleCondition);
        ret.addProperty("property", this.property);
        ret.addProperty("helpText", this.helpText);

        if (this.defaultValue != null && this.defaultValue.length() > 0) {
            if (this.propertyType.equals(Boolean.class) || this.propertyType.equals(boolean.class)) {
                ret.addProperty("defaultValue", Boolean.valueOf(this.defaultValue));
            } else if (this.propertyType.equals(Integer.class) || this.propertyType.equals(int.class)) {
                ret.addProperty("defaultValue", Integer.valueOf(this.defaultValue));
            } else {
                ret.addProperty("defaultValue", this.defaultValue);
            }
        } else {
            ret.addProperty("defaultValue", this.defaultValue);
        }

        JsonArray array = new JsonArray();
        
        if (this.selectValues != null) {
            for (SelectValueModel selectValue : this.selectValues) {
                JsonObject o = new JsonObject();
                o.addProperty("text", selectValue.getText());
                o.addProperty("value", selectValue.getValue());
                array.add(o);
            }
        }
        
        ret.add("selectValues", array);
        
        return ret;
    }
}
