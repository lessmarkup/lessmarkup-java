package com.lessmarkup.interfaces.structure;

public enum NodeAccessType {
    NO_ACCESS(0),
    READ(1),
    WRITE(2),
    MANAGE(3);
    
    private int type;
    
    NodeAccessType(int type) {
        this.type = type;
    }
    
    public int getLevel() {
        return this.type;
    }
}
