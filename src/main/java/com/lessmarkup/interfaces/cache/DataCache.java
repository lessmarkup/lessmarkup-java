package com.lessmarkup.interfaces.cache;

import java.util.OptionalLong;

public interface DataCache {
    <T extends CacheHandler> T get(Class<T> type, OptionalLong objectId, boolean create);
    <T extends CacheHandler> T get(Class<T> type, OptionalLong objectId);
    <T extends CacheHandler> T get(Class<T> type);
    <T extends CacheHandler> void expired(Class<T> type, OptionalLong objectId);
    <T extends CacheHandler> T createWithUniqueId(Class<T> type);
    void reset();
}
