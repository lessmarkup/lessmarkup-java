package com.lessmarkup.interfaces.recordmodel;

import com.lessmarkup.framework.helpers.PropertyDescriptor;
import com.lessmarkup.framework.helpers.StringHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputFieldDefinition {

    private final PropertyDescriptor property;
    private final String id;
    private final boolean readOnly;
    private final InputFieldType type;
    private final String readOnlyCondition;
    private final boolean required;
    private final String textId;
    private final String visibleCondition;
    private final double width;
    private final int minWidth;
    private final int maxWidth;
    private final int position;
    private final String defaultValue;
    private List<InputFieldEnum> enumValues;
    
    public InputFieldDefinition(PropertyDescriptor property, InputField definition) {
        this.property = property;
        if (StringHelper.isNullOrEmpty(definition.id())) {
            this.id = StringHelper.toJsonCase(property.getName());
        } else {
            this.id = definition.id();
        }
        this.readOnly = definition.readOnly();
        this.readOnlyCondition = definition.readOnlyCondition();
        this.required = definition.required();
        this.textId = definition.textId();
        this.type = definition.type();
        this.visibleCondition = definition.visibleCondition();
        this.width = definition.width();
        this.minWidth = definition.minWidth();
        this.maxWidth = definition.maxWidth();
        this.position = definition.position();
        this.defaultValue = definition.defaultValue();

        if (property.getType().isEnum() &&
                (type == InputFieldType.SELECT || type == InputFieldType.SELECT_TEXT || type == InputFieldType.MULTI_SELECT) && 
                definition.enumTextIdBase() != null) {
            initializeEnum(property.getType(), definition.enumTextIdBase());
        }
    }
    
    private void initializeEnum(Class<?> enumType, String textIdBase) {
        
        this.enumValues = new ArrayList<>();
        
        Arrays.stream(enumType.getEnumConstants()).forEach(c -> {
            String enumTextId = textIdBase + c.toString();
            InputFieldEnum enumValue = new InputFieldEnum(enumTextId, c.toString());
            this.enumValues.add(enumValue);
        });
    }
    
    public String getId() { return id; }
    public PropertyDescriptor getProperty() { return property; }
    public boolean isRequired() { return required; }
    public InputFieldType getType() { return type; }
    public boolean isReadOnly() { return readOnly; }
    public String getVisibleCondition() { return visibleCondition; }
    public String getReadOnlyCondition() { return readOnlyCondition; }
    public String getTextId() { return textId; }
    public double getWidth() { return width; }
    public int getMinWidth() { return minWidth; }
    public int getMaxWidth() { return maxWidth; }
    public int getPosition() { return position; }
    public String getDefaultValue() { return defaultValue; }
    public List<InputFieldEnum> getEnumValues() { return enumValues; }
}
