/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import java.util.OptionalLong;

/**
 *
 * @author User
 */
public class NodeAccess extends AbstractDataObject {
    private long nodeId;
    private NodeAccessType accessType;
    private OptionalLong userId;
    private OptionalLong groupId;
    
    public long getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(long value) {
        nodeId = value;
    }

    /**
     * @return the accessType
     */
    public NodeAccessType getAccessType() {
        return accessType;
    }

    /**
     * @param accessType the accessType to set
     */
    public void setAccessType(NodeAccessType accessType) {
        this.accessType = accessType;
    }

    /**
     * @return the userId
     */
    public OptionalLong getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(OptionalLong userId) {
        this.userId = userId;
    }

    /**
     * @return the groupId
     */
    public OptionalLong getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(OptionalLong groupId) {
        this.groupId = groupId;
    }
}
