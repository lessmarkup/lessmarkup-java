package com.lessmarkup.interfaces.data;

import com.lessmarkup.interfaces.cache.CacheHandler;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.Predicate;

public interface ChangesCache extends CacheHandler {
    OptionalLong getLastChangeId();
    List<DataChange> getCollectionChanges(int collectionId, OptionalLong fromId, OptionalLong toId, Predicate<DataChange> filterFunc);
}
