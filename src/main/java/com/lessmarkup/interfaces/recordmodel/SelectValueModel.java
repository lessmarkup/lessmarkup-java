package com.lessmarkup.interfaces.recordmodel;

public class SelectValueModel {
    private final String text;
    private final String value;
    
    public SelectValueModel(String text, String value) {
        this.text = text;
        this.value = value;
    }
    
    public String getText() {
        return this.text;
    }
    
    public String getValue() {
        return this.value;
    }
}
