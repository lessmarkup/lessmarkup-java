package com.lessmarkup.interfaces.module;

public class UserProperty {
    private String name;
    private Object value;
    private int type;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    
    
}
