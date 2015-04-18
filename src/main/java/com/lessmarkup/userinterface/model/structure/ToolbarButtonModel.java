package com.lessmarkup.userinterface.model.structure;

public class ToolbarButtonModel {
    private final String id;
    private final String text;
    
    public ToolbarButtonModel(String id, String text) {
        this.id = id;
        this.text = text;
    }
    
    public String getId() {
        return this.id;
    }
    
    public String getText() {
        return this.text;
    }
}
