package com.lessmarkup.engine.data;

import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.dataobjects.EntityChangeHistory;
import com.lessmarkup.interfaces.cache.AbstractCacheHandler;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangesCache;
import com.lessmarkup.interfaces.data.DataChange;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.data.QueryBuilder;
import com.lessmarkup.interfaces.module.Implements;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

class Change implements DataChange {
    private long id;
    private long entityId;
    private OffsetDateTime created;
    private OptionalLong userId;
    private long parameter1;
    private long parameter2;
    private long parameter3;
    private EntityChangeType type;

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    @Override
    public long getEntityId() {
        return entityId;
    }
    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }
    @Override
    public OffsetDateTime getCreated() {
        return created;
    }
    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }
    @Override
    public OptionalLong getUserId() {
        return userId;
    }
    public void setUserId(OptionalLong userId) {
        this.userId = userId;
    }
    @Override
    public long getParameter1() {
        return parameter1;
    }
    public void setParameter1(long parameter1) {
        this.parameter1 = parameter1;
    }
    @Override
    public long getParameter2() {
        return parameter2;
    }
    public void setParameter2(long parameter2) {
        this.parameter2 = parameter2;
    }
    @Override
    public long getParameter3() {
        return parameter3;
    }
    public void setParameter3(long parameter3) {
        this.parameter3 = parameter3;
    }
    @Override
    public EntityChangeType getType() {
        return type;
    }
    public void setType(EntityChangeType type) {
        this.type = type;
    }
}

@Implements(ChangesCache.class)
class ChangesCacheImpl extends AbstractCacheHandler implements ChangesCache {

    private OptionalLong lastUpdateId = OptionalLong.empty();
    private long lastUpdateTime;
    private final DomainModelProvider domainModelProvider;
    private static final int UPDATE_INTERVAL = 500;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Integer, List<Change>> changes = new HashMap<>();

    @Inject
    public ChangesCacheImpl(DomainModelProvider domainModelProvider) {
        super(null);
        this.domainModelProvider = domainModelProvider;
    }
    
    private void updateIfRequired() {
        if (System.currentTimeMillis() - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        
        lock.writeLock().lock();
        
        try {
            lastUpdateTime = System.currentTimeMillis();
            
            try (DomainModel domainModel = domainModelProvider.create()) {
                OffsetDateTime dateFrame = OffsetDateTime.now().minusDays(1);
                
                QueryBuilder query = domainModel.query().from(EntityChangeHistory.class);
                
                if (!lastUpdateId.isPresent()) {
                    query = query.where("created >= $", dateFrame);
                } else {
                    query = query.where("created >= $ AND " + Constants.DataIdPropertyName() + " > $", dateFrame, lastUpdateId.getAsLong());
                }
                
                query.toList(EntityChangeHistory.class).forEach(history -> {
                    lastUpdateId = OptionalLong.of(history.getId());
                    List<Change> collection = changes.get(history.getCollectionId());
                    if (collection == null) {
                        collection = new LinkedList<>();
                        changes.put(history.getCollectionId(), collection);
                    }
                    
                    Change change = new Change();
                    change.setId(history.getId());
                    change.setEntityId(history.getEntityId());
                    change.setCreated(history.getCreated());
                    change.setType(EntityChangeType.of(history.getChangeType()));
                    change.setUserId(history.getUserId());
                    change.setParameter1(history.getParameter1());
                    change.setParameter2(history.getParameter2());
                    change.setParameter3(history.getParameter3());
                    
                    collection.add(change);
                    
                    if (collection.get(0).getCreated().isBefore(dateFrame)) {
                        collection.removeIf(h -> h.getCreated().isBefore(dateFrame));
                    }
                });
                
            } catch (Exception ex) {
                Logger.getLogger(ChangesCacheImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public OptionalLong getLastChangeId() {
        updateIfRequired();
        return lastUpdateId;
    }

    @Override
    public List<DataChange> getCollectionChanges(int collectionId, OptionalLong fromId, OptionalLong toId, Predicate<DataChange> filterFunc) {
        updateIfRequired();
        
        lock.readLock().lock();
        try {
            
            List<Change> collection = changes.get(collectionId);
            if (collection == null) {
                return null;
            }
            
            Stream<Change> query = collection.stream();
            
            if (fromId.isPresent()) {
                query = query.filter(c -> c.getId() > fromId.getAsLong());
            }
            
            if (toId.isPresent()) {
                query = query.filter(c -> c.getId() <= toId.getAsLong());
            }
            
            if (filterFunc != null) {
                query = query.filter(filterFunc);
            }
            
            List<DataChange> ret = new ArrayList<>();
            query.forEach(ret::add);
            return ret;
            
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void initialize(OptionalLong objectId) {
    }
}
