package com.lessmarkup.userinterface.nodehandlers.common;

import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangesCache;
import com.lessmarkup.interfaces.data.DataChange;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.recordmodel.ModelCollection;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.structure.NotificationProvider;

import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;

public abstract class RecordListWithNotifyNodeHandler<T extends RecordModel> extends RecordListNodeHandler<T> implements NotificationProvider {

    private final DataCache dataCache;
    private final CurrentUser currentUser;

    protected RecordListWithNotifyNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache, CurrentUser currentUser, Class<T> modelType) {
        super(domainModelProvider, dataCache, modelType);
        this.dataCache = dataCache;
        this.currentUser = currentUser;
    }

    @Override
    public abstract String getTitle();

    @Override
    public abstract String getTooltip();

    @Override
    public abstract String getIcon();

    @Override
    public int getValueChange(OptionalLong fromVersion, OptionalLong toVersion, DomainModel domainModel) {
        ChangesCache changesCache = dataCache.get(ChangesCache.class);
        OptionalLong userId = currentUser.getUserId();

        ModelCollection<T> collection = getCollection();

        List<DataChange> changes = changesCache.getCollectionChanges(collection.getCollectionId(), fromVersion, toVersion, change -> {
            if (userId.isPresent() && change.getUserId() == userId) {
                return false;
            }
            return change.getType() != EntityChangeType.REMOVED;
        });

        if (changes == null) {
            return 0;
        }

        Set<Long> changeIds = new HashSet<>();
        changes.forEach(c -> changeIds.add(c.getEntityId()));

        return collection.readIds(domainModel.query().whereIds(changeIds), true).size();
    }

    @Override
    protected boolean isSupportsLiveUpdates() {
        return false;
    }

    @Override
    protected boolean isSupportsManualRefresh() {
        return false;
    }
}
