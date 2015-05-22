package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.recordmodel.PropertyCollectionManager;
import com.lessmarkup.interfaces.structure.CachedNodeInformation;
import com.lessmarkup.interfaces.structure.ChildHandlerSettings;
import com.lessmarkup.interfaces.structure.NodeCache;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TypeaheadModel {
    
    private final DataCache dataCache;
    private final DomainModelProvider domainModelProvider;
    private List<String> records;

    @Inject
    public TypeaheadModel(DataCache dataCache, DomainModelProvider domainModelProvider) {
        this.dataCache = dataCache;
        this.domainModelProvider = domainModelProvider;
    }
    
    
    public void initialize(String path, String property, String searchText) {
        NodeCache nodeCache = this.dataCache.get(NodeCache.class);
        
        Tuple<CachedNodeInformation,String> node = nodeCache.getNode(path);
        
        if (node == null) {
            throw new IllegalArgumentException("Cannot find node");
        }
        
        NodeHandler handler = DependencyResolver.resolve(node.getValue1().getHandlerType());
        
        List<PropertyCollectionManager> collectionManagers = new ArrayList<>();
        if (PropertyCollectionManager.class.isAssignableFrom(node.getValue1().getHandlerType())) {
            collectionManagers.add((PropertyCollectionManager) handler);
        }
        
        String rest = node.getValue2();
        
        while (rest != null && rest.length() > 0) {
            ChildHandlerSettings childSettings = handler.getChildHandler(rest);
            if (childSettings == null || !childSettings.getId().isPresent()) {
                throw new IllegalArgumentException();
            }
            handler = childSettings.getHandler();
            if (PropertyCollectionManager.class.isAssignableFrom(handler.getClass())) {
                collectionManagers.add((PropertyCollectionManager) handler);
            }
            
            rest = childSettings.getRest();
        }
        
        if (!collectionManagers.isEmpty()) {
            try (DomainModel domainModel = this.domainModelProvider.create()) {
                for (PropertyCollectionManager manager : collectionManagers) {
                    List<String> collection = manager.getCollection(domainModel, property, searchText);
                    if (collection == null || collection.isEmpty()) {
                        continue;
                    }
                    
                    records = new ArrayList<>();
                    collection.stream().limit(10).forEach(records::add);
                    return;
                }
            } catch (Exception ex) {
                Logger.getLogger(TypeaheadModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public JsonObject toJson() {
        JsonObject builder = new JsonObject();
        JsonArray array = new JsonArray();
        if (this.records != null) {
            for (String record : records) {
                array.add(new JsonPrimitive(record));
            }
        }
        builder.add("records", array);
        return builder;
    }
}
