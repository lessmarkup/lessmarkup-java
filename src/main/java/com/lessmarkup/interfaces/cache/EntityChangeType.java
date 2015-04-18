package com.lessmarkup.interfaces.cache;

public enum EntityChangeType {
    ADDED(1),
    REMOVED(2),
    UPDATED(3);
    
    private final int type;
    
    EntityChangeType(int type) {
        this.type = type;
    }
    
    public int getChangeType() {
        return this.type;
    }
    
    public static EntityChangeType of(int type) {
        switch (type) {
            case 1:
                return ADDED;
            case 2:
                return REMOVED;
            case 3:
                return UPDATED;
            default:
                return null;
        }
    }
}
