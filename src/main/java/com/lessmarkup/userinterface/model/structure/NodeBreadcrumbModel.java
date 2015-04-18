package com.lessmarkup.userinterface.model.structure;

public class NodeBreadcrumbModel {
    
    private final String text;
    private final String url;
    
    public NodeBreadcrumbModel(String text, String url) {
        this.text = text;
        this.url = url;
    }
    
    public String getText() {
        return this.text;
    }
    
    public String getUrl() {
        return this.url;
    }
}
