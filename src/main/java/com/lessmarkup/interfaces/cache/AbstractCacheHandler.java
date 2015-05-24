package com.lessmarkup.interfaces.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.OptionalLong;

public abstract class AbstractCacheHandler implements CacheHandler {

    private final Collection<Class<?>> handledCollectionTypes;
    
    protected AbstractCacheHandler(Class<?>[] handledCollectionTypes)
    {
        this.handledCollectionTypes = new ArrayList<>();    
        if (handledCollectionTypes != null) {
            Arrays.stream(handledCollectionTypes).forEach(this.handledCollectionTypes::add);
        }
    }
    
    @Override
    public void initialize(OptionalLong objectId) {
        if (objectId.isPresent()) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean expires(int collectionId, long entityId, EntityChangeType changeType) {
        return true;
    }

    @Override
    public Collection<Class<?>> getHandledCollectionTypes() {
        return handledCollectionTypes;
    }

    @Override
    public boolean isExpired() {
        return false;
    }
    
}
