package com.lessmarkup.interfaces.data;

import com.lessmarkup.interfaces.cache.EntityChangeType;
import java.util.OptionalLong;

public interface ChangeListener {
    void onChange(long recordId, OptionalLong userId, long entityId, int collectionId, EntityChangeType changeType);
}
