/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.security;

import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.dataobjects.UserGroupMembership;
import com.lessmarkup.interfaces.cache.AbstractCacheHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.module.Implements;
import com.lessmarkup.interfaces.structure.CachedNodeInformation;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import com.lessmarkup.interfaces.structure.NodeCache;
import com.lessmarkup.interfaces.structure.Tuple;
import com.lessmarkup.interfaces.system.UserCache;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

@Implements(UserCache.class)
public class UserCacheImpl extends AbstractCacheHandler implements UserCache {

    private OptionalLong userId;
    private final DomainModelProvider domainModelProvider;
    private final DataCache dataCache;
    
    private final List<Long> groups = new ArrayList<>();
    private boolean isRemoved;
    private boolean isAdministrator;
    private boolean isApproved;
    private boolean emailConfirmed;
    private String email;
    private String title;
    private boolean isBlocked;
    private OffsetDateTime unblockTime = null;
    private String properties;
    private OptionalLong avatarImageId;
    private OptionalLong userImageId;
    private String name;
    private final List<Tuple<CachedNodeInformation, NodeAccessType>> nodes = new ArrayList<>();

    @Inject
    public UserCacheImpl(DomainModelProvider domainModelProvider, DataCache dataCache) {
        super(new Class<?>[] { User.class });
        this.domainModelProvider = domainModelProvider;
        this.dataCache = dataCache;
    }
    
    private void initializeUser() {
        try (DomainModel domainModel = domainModelProvider.create()) {
            
            User user = domainModel.query().from(User.class).where(Constants.Data.ID_PROPERTY_NAME + " = $", userId.getAsLong()).firstOrDefault(User.class);

            if (user == null) {
                userId = OptionalLong.empty();
                return;
            }

            isAdministrator = user.isAdministrator();
            
            domainModel.query()
                    .from(UserGroupMembership.class)
                    .where("userId = $", userId.getAsLong())
                    .toList(UserGroupMembership.class, "UserGroupId")
                    .forEach(g -> { groups.add(g.getId()); });
            email = user.getEmail();
            title = user.getTitle();
            emailConfirmed = user.isEmailConfirmed();
            isBlocked = user.isBlocked();
            isApproved = user.isApproved();
            unblockTime = user.getUnblockTime();
            properties = user.getProperties();
            name = user.getName();
            avatarImageId = user.getAvatarImageId();
            userImageId = user.getUserImageId();

            if (isBlocked && unblockTime != null && unblockTime.isBefore(OffsetDateTime.now())) {
                isBlocked = false;
            }
        }
    }
    
    @Override
    public void initialize(OptionalLong objectId) {
        userId = objectId;
        
        if (userId.isPresent()) {
            initializeUser();
        }
        
        dataCache.get(NodeCache.class).getNodes().stream()
                .forEach(node -> { 
                    NodeAccessType type = node.checkRights(this, userId);
                    if (type == NodeAccessType.NO_ACCESS) {
                        return;
                    }
                    nodes.add(new Tuple<>(node, type));
                });
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRemoved() {
        return isRemoved;
    }

    @Override
    public boolean isAdministrator() {
        return isAdministrator;
    }

    @Override
    public boolean isApproved() {
        return isApproved;
    }

    @Override
    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    @Override
    public List<Long> getGroups() {
        return groups;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean isBlocked() {
        return isBlocked;
    }

    @Override
    public String getProperties() {
        return properties;
    }

    @Override
    public OptionalLong getAvatarImageId() {
        return avatarImageId;
    }

    @Override
    public OptionalLong getUserImageId() {
        return userImageId;
    }

    @Override
    public List<Tuple<CachedNodeInformation, NodeAccessType>> getNodes() {
        return nodes;
    }

    @Override
    public OffsetDateTime getUnblockTime() {
        return unblockTime;
    }
    
}
