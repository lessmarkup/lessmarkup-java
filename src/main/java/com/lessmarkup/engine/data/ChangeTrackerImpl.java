package com.lessmarkup.engine.data;

import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.dataobjects.EntityChangeHistory;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeListener;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.module.Implements;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

@Implements(ChangeTracker.class)
class ChangeTrackerImpl implements ChangeTracker {

    private boolean changeTrackingInitialized;
    private final DomainModelProvider domainModelProvider;
    private final Object syncObject = new Object();
    private long lastUpdateId;
    private final Queue<EntityChangeHistory> changeQueue = new LinkedList<>();
    private final List<ChangeListener> listeners = new ArrayList<>();

    @Inject
    public ChangeTrackerImpl(DomainModelProvider domainModelProvider) {
        this.domainModelProvider = domainModelProvider;
    }
    
    private void handleQueue() {
        if (listeners.isEmpty()) {
            synchronized(syncObject) {
                if (listeners.isEmpty()) {
                    changeQueue.clear();
                    return;
                }
            }
        }
        
        for (;;) {
            EntityChangeHistory change;
            synchronized(syncObject) {
                change = changeQueue.poll();
                if (change == null) {
                    break;
                }
            }
            listeners.stream().forEach(listener -> {
                listener.onChange(change.getId(), change.getUserId(), change.getEntityId(), change.getCollectionId(), EntityChangeType.of(change.getChangeType()));
            });
        }
    }
    
    public void stop() {
    }
    
    private void initializeChangeTracker() {
        if (changeTrackingInitialized) {
            return;
        }
        
        synchronized(syncObject) {
            if (changeTrackingInitialized) {
                return;
            }
            
            changeTrackingInitialized = true;
            
            try (DomainModel domainModel = domainModelProvider.create()) {
                EntityChangeHistory history = domainModel.query().from(EntityChangeHistory.class).orderByDescending(Constants.Data.ID_PROPERTY_NAME).firstOrDefault(EntityChangeHistory.class);
                if (history != null) {
                    lastUpdateId = history.getId();
                }
            } catch (Exception ex) {
                Logger.getLogger(ChangeTrackerImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public void invalidate() {
        enqueueUpdates();
        handleQueue();
    }

    @Override
    public <T extends DataObject> void addChange(Class<T> type, long objectId, EntityChangeType changeType, DomainModel domainModel) {
        EntityChangeHistory record = new EntityChangeHistory();
        record.setEntityId(objectId);
        record.setChangeType(changeType.ordinal());
        record.setUserId(RequestContextHolder.getContext().getCurrentUser().getUserId());
        record.setCollectionId(DomainModelImpl.getCollectionId(type).getAsInt());
        record.setCreated(OffsetDateTime.now());
        domainModel.create(record);
    }

    @Override
    public <T extends DataObject> void addChange(Class<T> type, T dataObject, EntityChangeType changeType, DomainModel domainModel) {
        initializeChangeTracker();
        EntityChangeHistory record = new EntityChangeHistory();
        record.setEntityId(dataObject.getId());
        record.setChangeType(changeType.ordinal());
        record.setUserId(RequestContextHolder.getContext().getCurrentUser().getUserId());
        record.setCollectionId(DomainModelImpl.getCollectionId(type).getAsInt());
        record.setCreated(OffsetDateTime.now());
        domainModel.create(record);
    }

    @Override
    public void subscribe(ChangeListener listener) {
        initializeChangeTracker();
        listeners.add(listener);
    }

    @Override
    public void unsubscribe(ChangeListener listener) {
        initializeChangeTracker();
        listeners.remove(listener);
    }

    @Override
    public void enqueueUpdates() {
        try (DomainModel domainModel = domainModelProvider.create()) {
            domainModel.query().from(EntityChangeHistory.class).where("id > " + lastUpdateId).orderBy("id").toList(EntityChangeHistory.class).forEach(h -> {
                lastUpdateId = h.getId();
                synchronized(syncObject) {
                    changeQueue.add(h);
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(ChangeTrackerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
