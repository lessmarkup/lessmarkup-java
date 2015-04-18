package com.lessmarkup.interfaces.data;

public abstract class AbstractDataObject implements DataObject {
    
    private long id;
    
    public long getId() {
        return this.id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
}
