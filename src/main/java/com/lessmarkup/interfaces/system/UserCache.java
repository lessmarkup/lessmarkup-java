package com.lessmarkup.interfaces.system;

import com.lessmarkup.interfaces.cache.CacheHandler;
import com.lessmarkup.interfaces.structure.CachedNodeInformation;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import com.lessmarkup.interfaces.structure.Tuple;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.OptionalLong;

public interface UserCache extends CacheHandler {
    String getName();
    boolean isRemoved();
    boolean isAdministrator();
    boolean isApproved();
    boolean isEmailConfirmed();
    List<Long> getGroups();
    String getEmail();
    String getTitle();
    boolean isBlocked();
    OffsetDateTime getUnblockTime();
    String getProperties();
    OptionalLong getAvatarImageId();
    OptionalLong getUserImageId();
    List<Tuple<CachedNodeInformation, NodeAccessType>> getNodes();
}
