package com.lessmarkup.interfaces.recordmodel;

import com.lessmarkup.interfaces.cache.CacheHandler;

public interface RecordModelCache extends CacheHandler {
    RecordModelDefinition getDefinition(Class<?> type);
    RecordModelDefinition getDefinition(String id);
    boolean hasDefinition(Class<?> type);
}
