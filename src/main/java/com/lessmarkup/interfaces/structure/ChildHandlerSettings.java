package com.lessmarkup.interfaces.structure;

import java.util.OptionalLong;

public class ChildHandlerSettings {
    
    private NodeHandler handler;
    private OptionalLong id;
    private String title;
    private String path;
    private String rest;
    
    public NodeHandler getHandler() {
        return this.handler;
    }
    
    public void setHandler(NodeHandler handler) {
        this.handler = handler;
    }
    
    public OptionalLong getId() {
        return this.id;
    }
    
    public void setId(OptionalLong value) {
        this.id = value;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String value) {
        this.title = value;
    }
    
    public String getPath() {
        return this.path;
    }
    
    public void setPath(String value) {
        this.path = value;
    }
    
    public String getRest() {
        return this.rest;
    }
    
    public void setRest(String value) {
        this.rest = value;
    }
}
