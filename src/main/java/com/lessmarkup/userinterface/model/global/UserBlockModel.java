package com.lessmarkup.userinterface.model.global;

import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.dataobjects.UserBlockHistory;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.OptionalLong;

@Component
@Scope("prototype")
public class UserBlockModel extends RecordModel<UserBlockModel> {

    private String reason;
    private String internalReason;
    private OffsetDateTime unblockTime;

    private final DomainModelProvider domainModelProvider;
    private final ChangeTracker changeTracker;

    @Autowired
    public UserBlockModel(DomainModelProvider domainModelProvider, ChangeTracker changeTracker) {
        super(TextIds.BLOCK_USER);
        this.domainModelProvider = domainModelProvider;
        this.changeTracker = changeTracker;
    }

    public void blockUser(long userId) {
        OptionalLong currentUserId = RequestContextHolder.getContext().getCurrentUser().getUserId();
        if (!currentUserId.isPresent()) {
            throw new IllegalArgumentException();
        }

        try (DomainModel domainModel = domainModelProvider.create()) {
            User user = domainModel.query().from(User.class).find(User.class, userId);
            if (user == null) {
                throw new IllegalArgumentException();
            }
            user.setBlocked(true);
            user.setBlockReason(reason);
            if (unblockTime != null && unblockTime.isBefore(OffsetDateTime.now())) {
                unblockTime = null;
            }
            user.setUnblockTime(unblockTime);
            user.setLastBlock(OffsetDateTime.now());

            UserBlockHistory blockHistory = new UserBlockHistory();
            blockHistory.setBlockedByUserId(currentUserId.getAsLong());
            blockHistory.setBlockedToTime(unblockTime);
            blockHistory.setReason(reason);
            blockHistory.setInternalReason(internalReason);
            blockHistory.setUserId(userId);
            blockHistory.setCreated(OffsetDateTime.now());

            domainModel.create(blockHistory);
            changeTracker.addChange(User.class, user, EntityChangeType.UPDATED, domainModel);
        }
    }

    public void unblockUser(long userId) {
        try (DomainModel domainModel = domainModelProvider.create()) {
            User user = domainModel.query().from(User.class).find(User.class, userId);
            if (user == null || !user.isBlocked()) {
                return;
            }

            user.setBlocked(false);

            for (UserBlockHistory history : domainModel.query().from(UserBlockHistory.class).where("userId = $ AND unblocked = $", userId, false).toList(UserBlockHistory.class)) {
                history.setUnblocked(true);
                domainModel.update(history);
            }

            changeTracker.addChange(User.class, user, EntityChangeType.UPDATED, domainModel);

        }
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

    public OffsetDateTime getUnblockTime() {
        return unblockTime;
    }

    public void setUnblockTime(OffsetDateTime unblockTime) {
        this.unblockTime = unblockTime;
    }
}
