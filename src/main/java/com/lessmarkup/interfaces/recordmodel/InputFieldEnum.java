package com.lessmarkup.interfaces.recordmodel;

public class InputFieldEnum {
    private final String textId;
    private final String value;
    
    public InputFieldEnum(String textId, String value) {
        this.textId = textId;
        this.value = value;
    }
    
    public String getTextId() { return textId; }
    public String getValue() { return value; }
}
