package com.lessmarkup.engine.cache;

import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.CacheHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeListener;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.Tuple;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalLong;
import java.util.Random;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

class CacheItem {
    private final Class<?> type;
    private final OptionalLong objectId;
    private final CacheHandler cachedObject;
    
    public CacheItem(Class<?> type, OptionalLong objectId, CacheHandler cacheHandler) {
        this.type = type;
        this.objectId = objectId;
        this.cachedObject = cacheHandler;
    }
    
    public Class<?> getType() { return type; }
    
    public OptionalLong getObjectId() { return objectId; }
    
    public CacheHandler getCachedObject() { return cachedObject; }
}

@Component
public class DataCacheImpl implements DataCache, ChangeListener {

    private final HashMap<Integer, List<CacheItem>> hashedCollectionIds = new HashMap<>();
    private final HashMap<Tuple<Class<?>, OptionalLong>, CacheItem> items = new HashMap<>();
    private final ChangeTracker changeTracker;
    private boolean initialized = false;
    
    @Autowired
    public DataCacheImpl(ChangeTracker changeTracker) {
        this.changeTracker = changeTracker;
    }
    
    private synchronized void initialize() {
        if (initialized) {
            return;
        }
        this.changeTracker.subscribe(this);
        initialized = true;
    }

    private void checkInitialize() {
        if (!initialized) {
            initialize();
        }
    }
    
    @PreDestroy public void close() {
        if (initialized) {
            this.changeTracker.unsubscribe(this);
        }
    }
    
    synchronized private <T extends CacheHandler> void set(Class<T> type, T cachedObject, OptionalLong objectId) {

        checkInitialize();

        Tuple<Class<?>, OptionalLong> key = new Tuple<>(type, objectId);
        CacheItem cacheItem = new CacheItem(type, objectId, cachedObject);
        
        boolean exists = this.items.containsKey(key);
        
        items.put(key, cacheItem);
        
        if (exists) {
            return;
        }
        
        Collection<Class<?>> collectionTypes = cachedObject.getHandledCollectionTypes();
        
        if (collectionTypes != null) {
            
            DomainModelProvider domainModelProvider = DependencyResolver.resolve(DomainModelProvider.class);
            
            for (Class<?> collectionType : collectionTypes) {
                int collectionId = domainModelProvider.getCollectionId(collectionType);
                List<CacheItem> collectionHandlers = this.hashedCollectionIds.get(collectionId);
                if (collectionHandlers == null) {
                    collectionHandlers = new ArrayList<>();
                    this.hashedCollectionIds.put(collectionId, collectionHandlers);
                }
                collectionHandlers.add(cacheItem);
            }
        }
    }
    
    @Override
    synchronized public <T extends CacheHandler> T get(Class<T> type, OptionalLong objectId, boolean create) {

        checkInitialize();

        Tuple<Class<?>, OptionalLong> key = new Tuple<>(type, objectId);
        CacheItem ret = this.items.get(key);
        if (ret != null) {
            if (ret.getCachedObject().isExpired()) {
                this.items.remove(key);
            } else {
                CacheHandler obj = ret.getCachedObject();
                if (type.isInstance(obj)) {
                    return type.cast(obj);
                } else {
                    return null;
                }
            }
        }
        
        if (!create) {
            return null;
        }
        
        T newObject = DependencyResolver.resolve(type);
        set(type, newObject, objectId);
        try {
            newObject.initialize(objectId);
        } catch (Exception e) {
            remove(key);
            throw e;
        }
        ret = items.get(key);
        CacheHandler obj = ret.getCachedObject();
        if (type.isInstance(obj)) {
            return type.cast(obj);
        } else {
            return null;
        }
    }

    @Override
    public <T extends CacheHandler> T get(Class<T> type, OptionalLong objectId) {
        return get(type, objectId, true);
    }

    @Override
    public <T extends CacheHandler> T get(Class<T> type) {
        return get(type, OptionalLong.empty());
    }

    @Override
    synchronized public <T extends CacheHandler> void expired(Class<T> type, OptionalLong objectId) {
        remove(new Tuple<>(type, objectId));
    }

    @Override
    synchronized public <T extends CacheHandler> T createWithUniqueId(Class<T> type) {
        Random random = new Random();
        
        for (;;) {
            OptionalLong objectId = OptionalLong.of(random.nextLong());
            Tuple<Class<?>, OptionalLong> key = new Tuple<>(type, objectId);
            if (!this.items.containsKey(key)) {
                return get(type, objectId);
            }
        }
    }

    @Override
    synchronized public void reset() {
        this.hashedCollectionIds.clear();
        this.items.clear();
    }
 
    private void remove(Tuple<Class<?>, OptionalLong> key) {
        CacheItem cacheItem = this.items.get(key);
        
        if (cacheItem == null) {
            return;
        }
        
        DomainModelProvider domainModelProvider = DependencyResolver.resolve(DomainModelProvider.class);
        
        for (Class<?> type : cacheItem.getCachedObject().getHandledCollectionTypes()) {
            int collectionId = domainModelProvider.getCollectionId(type);
            List<CacheItem> collectionHandlers = this.hashedCollectionIds.get(collectionId);
            if (collectionHandlers != null) {
                collectionHandlers.remove(cacheItem);
            }
        }
        
        this.items.remove(key);
    }

    @Override
    public void onChange(long recordId, OptionalLong userId, long entityId, int collectionId, EntityChangeType changeType) {
        List<CacheItem> handlers = this.hashedCollectionIds.get(collectionId);
        
        if (handlers == null) {
            return;
        }
        
        List<CacheItem> itemsToRemove = new ArrayList<>();
        handlers.stream().filter(ci -> ci.getCachedObject().expires(collectionId, entityId, changeType)).forEach(itemsToRemove::add);
        
        if (itemsToRemove.isEmpty()) {
            return;
        }
        
        itemsToRemove.stream().forEach(item -> remove(new Tuple<>(item.getType(), item.getObjectId())));
    }
}
