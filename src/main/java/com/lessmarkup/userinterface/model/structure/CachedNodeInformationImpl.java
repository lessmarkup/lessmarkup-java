package com.lessmarkup.userinterface.model.structure;

import com.lessmarkup.dataobjects.Node;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.structure.CachedNodeAccess;
import com.lessmarkup.interfaces.structure.CachedNodeInformation;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.system.UserCache;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

public class CachedNodeInformationImpl implements CachedNodeInformation {

    private long nodeId;
    private boolean enabled;
    private String path;
    private int order;
    private int level;
    private String title;
    private String description;
    private String handlerId;
    private CachedNodeInformation parent;
    private final List<CachedNodeAccess> accessList = new ArrayList<>();
    private final List<CachedNodeInformation> children = new ArrayList<>();
    private String fullPath;
    private String handlerModuleType;
    private String settings;
    private CachedNodeInformation root;
    private boolean visible;
    private boolean addToMenu;
    private boolean loggedIn;
    private OptionalLong parentNodeId = OptionalLong.empty();
    private Class<? extends NodeHandler> handlerType;
    
    public CachedNodeInformationImpl(Node node) {
        this.nodeId = node.getId();
    }
    
    public CachedNodeInformationImpl() {
        
    }
    
    @Override
    public long getNodeId() {
        return this.nodeId;
    }
    
    public void setNodeId(long value) {
        this.nodeId = value;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    @Override
    public String getPath() {
        return this.path;
    }
    
    public void setPath(String value) {
        this.path = value;
    }

    @Override
    public int getOrder() {
        return this.order;
    }
    
    public void setOrder(int value) {
        this.order = value;
    }

    @Override
    public int getLevel() {
        return this.level;
    }
    
    public void setLevel(int value) {
        this.level = value;
    }

    @Override
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String value) {
        this.title = value;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String value) {
        this.description = value;
    }

    @Override
    public String getHandlerId() {
        return this.handlerId;
    }
    
    public void setHandlerId(String value) {
        this.handlerId = value;
    }

    @Override
    public CachedNodeInformation getParent() {
        return this.parent;
    }
    
    public void setParent(CachedNodeInformation value) {
        this.parent = value;
    }

    @Override
    public List<CachedNodeAccess> getAccessList() {
        return this.accessList;
    }

    @Override
    public List<CachedNodeInformation> getChildren() {
        return this.children;
    }

    @Override
    public String getFullPath() {
        return this.fullPath;
    }
    
    public void setFullPath(String value) {
        this.fullPath = value;
    }

    @Override
    public String getHandlerModuleType() {
        return this.handlerModuleType;
    }
    
    public void setHandlerModuleType(String value) {
        this.handlerModuleType = value;
    }

    @Override
    public String getSettings() {
        return this.settings;
    }
    
    public void setSettings(String value) {
        this.settings = value;
    }

    @Override
    public CachedNodeInformation getRoot() {
        return this.root;
    }
    
    public void setRoot(CachedNodeInformation value) {
        this.root = value;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }
    
    public void setVisible(boolean value) {
        this.visible = value;
    }

    @Override
    public boolean isAddToMenu() {
        return this.addToMenu;
    }
    
    public void setAddToMenu(boolean value) {
        this.addToMenu = value;
    }

    @Override
    public boolean isLoggedIn() {
        return this.loggedIn;
    }
    
    public void setLoggedIn(boolean value) {
        this.loggedIn = value;
    }
    
    private static boolean appliesTo(CachedNodeAccess nodeAccess, OptionalLong userId, List<Long> groupIds) {
        if (!userId.isPresent()) {
            return !nodeAccess.getUserId().isPresent() && !nodeAccess.getGroupId().isPresent();
        }
        
        if (nodeAccess.getUserId().isPresent()) {
            if (!userId.isPresent()) {
                return false;
            }
            return nodeAccess.getUserId().getAsLong() == userId.getAsLong();
        }
        
        if (nodeAccess.getGroupId().isPresent()) {
            return groupIds != null && groupIds.stream().anyMatch(g -> g == nodeAccess.getUserId().getAsLong());
        }
        
        return false;
    }
    
    private NodeAccessType checkRights(OptionalLong userId, List<Long> groupIds, NodeAccessType accessType) {
        if (this.parent != null) {
            accessType = ((CachedNodeInformationImpl) this.parent).checkRights(userId, groupIds, accessType);
        }
        
        if (this.accessList.isEmpty()) {
            return accessType;
        }
        
        CachedNodeAccess maxAccess = null;
        
        for (CachedNodeAccess access : this.accessList) {
            if (maxAccess == null) {
                maxAccess = access;
                continue;
            }
            
            if (maxAccess.getAccessType().getLevel() < access.getAccessType().getLevel()) {
                maxAccess = access;
            }
        }
        
        if (maxAccess != null && (accessType == null || accessType.getLevel() > maxAccess.getAccessType().getLevel())) {
            accessType = maxAccess.getAccessType();
        }
        
        return accessType;
    }

    @Override
    public NodeAccessType checkRights(CurrentUser currentUser, NodeAccessType defaultAccessType) {
        if (currentUser.isAdministrator()) {
            return NodeAccessType.MANAGE;
        }
        
        NodeAccessType accessType = checkRights(currentUser.getUserIdJava(), currentUser.getGroupsJava(), defaultAccessType);
        
        if (accessType != null && accessType != NodeAccessType.NO_ACCESS && (!currentUser.isApproved() || !currentUser.emailConfirmed())) {
            accessType = NodeAccessType.READ;
        }
        
        return accessType;
    }

    @Override
    public NodeAccessType checkRights(UserCache userCache, OptionalLong userId, NodeAccessType defaultAccessType) {
        if (userCache.isAdministrator()) {
            return NodeAccessType.MANAGE;
        }
        
        NodeAccessType accessType = checkRights(userId, userCache.getGroupsJava(), defaultAccessType);
        
        if (accessType != null && accessType != NodeAccessType.NO_ACCESS && (!userCache.isApproved() || !userCache.isEmailConfirmed())) {
            accessType = NodeAccessType.READ;
        }
        
        return accessType;
    }

    @Override
    public OptionalLong getParentNodeId() {
        return this.parentNodeId;
    }
    
    public void setParentNodeId(OptionalLong value) {
        this.parentNodeId = value;
    }

    @Override
    public Class<? extends NodeHandler> getHandlerType() {
        return this.handlerType;
    }
    
    public void setHandlerType(Class<? extends NodeHandler> value) {
        this.handlerType = value;
    }
}
