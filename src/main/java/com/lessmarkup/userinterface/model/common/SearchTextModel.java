package com.lessmarkup.userinterface.model.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.text.SearchResults;
import com.lessmarkup.interfaces.text.TextSearch;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchTextModel {
    
    private final DomainModelProvider domainModelProvider;
    private final DataCache dataCache;
    
    @Inject
    public SearchTextModel(DataCache dataCache, DomainModelProvider domainModelProvider) {
        this.domainModelProvider = domainModelProvider;
        this.dataCache = dataCache;
    }
    
    public JsonElement handle(String searchText) {
        TextSearch searchCache = dataCache.get(TextSearch.class);
        
        try (DomainModel domainModel = this.domainModelProvider.create()) {
            SearchResults results = searchCache.search(searchText, 0, 10, domainModel);
            
            if (results == null) {
                return null;
            }
            
            JsonArray array = new JsonArray();
            
            results.getResults().forEach(r -> {
                JsonObject obj = new JsonObject();
                obj.add("name", new JsonPrimitive(r.getName()));
                obj.add("text", new JsonPrimitive(r.getText()));
                obj.add("url", new JsonPrimitive(r.getUrl()));
                array.add(obj);
            });
            
            return array;
        } catch (Exception ex) {
            Logger.getLogger(SearchTextModel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
