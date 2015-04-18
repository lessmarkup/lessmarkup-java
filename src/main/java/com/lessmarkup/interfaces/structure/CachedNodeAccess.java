package com.lessmarkup.interfaces.structure;

import com.lessmarkup.dataobjects.NodeAccess;
import java.util.OptionalLong;

public class CachedNodeAccess {
    private NodeAccessType accessType;
    private OptionalLong userId;
    private OptionalLong groupId;
    
    public CachedNodeAccess() {
    }
    
    public CachedNodeAccess(NodeAccess na) {
    }
    
    public NodeAccessType getAccessType() {
        return this.accessType;
    }
    
    public void setAccessType(NodeAccessType accessType) {
        this.accessType = accessType;
    }
    
    public OptionalLong getUserId() {
        return this.userId;
    }
    
    public void setUserId(OptionalLong userId) {
        this.userId = userId;
    }
    
    public OptionalLong getGroupId() {
        return this.groupId;
    }
    
    public void setGroupId(OptionalLong groupId) {
        this.groupId = groupId;
    }
}
