package com.lessmarkup.interfaces.structure;

import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.system.UserCache;
import java.util.List;
import java.util.OptionalLong;

public interface CachedNodeInformation {
    long getNodeId();
    boolean isEnabled();
    String getPath();
    int getOrder();
    int getLevel();
    String getTitle();
    String getDescription();
    String getHandlerId();
    OptionalLong getParentNodeId();
    CachedNodeInformation getParent();
    List<CachedNodeAccess> getAccessList();
    List<CachedNodeInformation> getChildren();
    String getFullPath();
    Class<? extends NodeHandler> getHandlerType();
    String getHandlerModuleType();
    String getSettings();
    CachedNodeInformation getRoot();
    boolean isVisible();
    boolean isAddToMenu();
    boolean isLoggedIn();
    NodeAccessType checkRights(CurrentUser currentUser, NodeAccessType defaultAccessType);
    default NodeAccessType checkRights(CurrentUser currentUser) {
        return checkRights(currentUser, NodeAccessType.READ);
    }
    NodeAccessType checkRights(UserCache userCache, OptionalLong userId, NodeAccessType defaultAccessType);
    default NodeAccessType checkRights(UserCache userCache, OptionalLong userId) {
        return checkRights(userCache, userId, NodeAccessType.READ);
    }
}
