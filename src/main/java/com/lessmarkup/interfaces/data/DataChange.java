package com.lessmarkup.interfaces.data;

import com.lessmarkup.interfaces.cache.EntityChangeType;

import java.time.OffsetDateTime;
import java.util.OptionalLong;

public interface DataChange {
    long getId();
    long getEntityId();
    OffsetDateTime getCreated();
    OptionalLong getUserId();
    long getParameter1();
    long getParameter2();
    long getParameter3();
    EntityChangeType getType();
}
