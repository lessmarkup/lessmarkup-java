package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

import java.time.OffsetDateTime;

public class UserBlockHistory extends AbstractDataObject {
    private long userId;
    private long blockedByUserId;
    @RequiredField
    private OffsetDateTime created;
    private OffsetDateTime blockedToTime;
    private boolean unblocked;
    @RequiredField
    private String reason;
    @RequiredField
    private String internalReason;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getBlockedByUserId() {
        return blockedByUserId;
    }

    public void setBlockedByUserId(long blockedByUserId) {
        this.blockedByUserId = blockedByUserId;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getBlockedToTime() {
        return blockedToTime;
    }

    public void setBlockedToTime(OffsetDateTime blockedToTime) {
        this.blockedToTime = blockedToTime;
    }

    public boolean isUnblocked() {
        return unblocked;
    }

    public void setUnblocked(boolean unblocked) {
        this.unblocked = unblocked;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getInternalReason() {
        return internalReason;
    }

    public void setInternalReason(String internalReason) {
        this.internalReason = internalReason;
    }
}
