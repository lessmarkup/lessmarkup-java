package com.lessmarkup.interfaces.recordmodel;

public class EnumSource {
    private final String text;
    private final String value;
    
    public EnumSource(String text, String value) {
        this.text = text;
        this.value = value;
    }
    
    public String getText() { return text; }
    public String getValue() { return value; }
}
