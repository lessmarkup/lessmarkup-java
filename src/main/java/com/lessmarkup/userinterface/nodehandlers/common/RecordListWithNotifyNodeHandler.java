package com.lessmarkup.userinterface.nodehandlers.common;

import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangesCache;
import com.lessmarkup.interfaces.data.DataChange;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.recordmodel.ModelCollection;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.structure.NotificationProvider;
import scala.Function1;

import java.util.*;

public abstract class RecordListWithNotifyNodeHandler<T extends RecordModel> extends RecordListNodeHandler<T> implements NotificationProvider {

    private final DataCache dataCache;

    protected RecordListWithNotifyNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache, Class<T> modelType) {
        super(domainModelProvider, dataCache, modelType);
        this.dataCache = dataCache;
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
        OptionalLong userId = RequestContextHolder.getContext().getCurrentUser().getUserId();

        ModelCollection<T> collection = getCollection();

        Collection<DataChange> changes = changesCache.getCollectionChanges(
                collection.getCollectionId(),
                fromVersion.isPresent() ? scala.Option.apply(fromVersion.getAsLong()) : scala.Option.empty(),
                toVersion.isPresent() ? scala.Option.apply(toVersion.getAsLong()) : scala.Option.empty(),
                scala.Option.apply(change -> {
                    return !(userId.isPresent()
                            && change.getUserId().isDefined()
                            && (Long) change.getUserId().get() == userId.getAsLong())
                            && change.getType() != EntityChangeType.REMOVED;
                }));

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
