package com.lessmarkup.interfaces.data;

import com.lessmarkup.interfaces.cache.EntityChangeType;

public interface ChangeTracker {
    void invalidate();
    <T extends DataObject> void addChange(Class<T> type, long objectId, EntityChangeType changeType, DomainModel domainModel);
    <T extends DataObject> void addChange(Class<T> type, T dataObject, EntityChangeType changeType, DomainModel domainModel);
    void subscribe(ChangeListener listener);
    void unsubscribe(ChangeListener listener);
    void enqueueUpdates();
}
