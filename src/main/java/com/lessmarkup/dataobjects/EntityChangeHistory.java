package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

import java.time.OffsetDateTime;
import java.util.OptionalLong;

public class EntityChangeHistory extends AbstractDataObject {
    private OptionalLong userId;
    private long entityId;
    private int collectionId;
    private int changeType;
    private OffsetDateTime created;
    private long parameter1;
    private long parameter2;
    private long parameter3;

    public OptionalLong getUserId() {
        return userId;
    }
    public void setUserId(OptionalLong userId) {
        this.userId = userId;
    }

    public long getEntityId() {
        return entityId;
    }
    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public int getCollectionId() {
        return collectionId;
    }
    public void setCollectionId(int collectionId) {
        this.collectionId = collectionId;
    }

    public int getChangeType() {
        return changeType;
    }
    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    @RequiredField
    public OffsetDateTime getCreated() {
        return created;
    }
    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public long getParameter1() {
        return parameter1;
    }
    public void setParameter1(long parameter1) {
        this.parameter1 = parameter1;
    }

    public long getParameter2() {
        return parameter2;
    }
    public void setParameter2(long parameter2) {
        this.parameter2 = parameter2;
    }

    public long getParameter3() {
        return parameter3;
    }
    public void setParameter3(long parameter3) {
        this.parameter3 = parameter3;
    }
}
