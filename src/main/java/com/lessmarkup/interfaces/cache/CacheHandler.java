package com.lessmarkup.interfaces.cache;

import java.util.Collection;
import java.util.OptionalLong;

public interface CacheHandler {
    void initialize(OptionalLong objectId);
    boolean expires(int collectionId, long entityId, EntityChangeType changeType);
    Collection<Class<?>> getHandledCollectionTypes();
    boolean isExpired();
}
