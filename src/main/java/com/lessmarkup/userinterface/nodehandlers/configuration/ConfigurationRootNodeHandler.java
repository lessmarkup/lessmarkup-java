package com.lessmarkup.userinterface.nodehandlers.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.structure.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalLong;

class ConfigurationGroupData {
    private final String title;
    private final List<ConfigurationHandlerData> handlers = new ArrayList<>();
    
    public ConfigurationGroupData(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public List<ConfigurationHandlerData> getHandlers() {
        return this.handlers;
    }
}

class ConfigurationHandlerData {
    private Class<? extends NodeHandler> type;
    private String titleTextId;
    private String title;
    private String moduleType;
    private long id;
    private String typeName;
    
    public Class<? extends NodeHandler> getType() {
        return this.type;
    }
    
    public void setType(Class<? extends NodeHandler> value) {
        this.type = value;
    }
    
    public String getTitleTextId() {
        return this.titleTextId;
    }
    
    public void setTitleTextId(String value) {
        this.titleTextId = value;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String value) {
        this.title = value;
    }
    
    public String getModuleType() {
        return this.moduleType;
    }
    
    public void setModuleType(String value) {
        this.moduleType = value;
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(long value) {
        this.id = value;
    }
    
    public String getTypeName() {
        return this.typeName;
    }
    
    public void setTypeName(String value) {
        this.typeName = value;
    }
}

public class ConfigurationRootNodeHandler extends AbstractNodeHandler {

    private final ModuleProvider moduleProvider;
    private final DataCache dataCache;
    private final List<ConfigurationGroupData> configurationGroups = new ArrayList<>();
    private final HashMap<String, ConfigurationHandlerData> configurationHandlers = new HashMap<>();

    @Inject
    public ConfigurationRootNodeHandler(ModuleProvider moduleProvider, DataCache dataCache) {
        this.moduleProvider = moduleProvider;
        this.dataCache = dataCache;
        
        addScript("scripts/controllers/ConfigurationController");
        
        long idCounter = 1;
        
        ConfigurationGroupData normalGroup = new ConfigurationGroupData(LanguageHelper.getFullTextId(Constants.ModuleType.MAIN, TextIds.SITE_CONFIGURATION));
        
        for (ModuleConfiguration module : this.moduleProvider.getModules()) {
            for (Class<? extends NodeHandler> nodeHandlerType : module.getInitializer().getNodeHandlerTypes()) {
                ConfigurationHandler configurationHandler = nodeHandlerType.getAnnotation(ConfigurationHandler.class);
                if (configurationHandler == null) {
                    continue;
                }
                String typeName = nodeHandlerType.getSimpleName().toLowerCase();
                if (typeName.endsWith("nodehandler")) {
                    typeName = typeName.substring(0, typeName.length()-"nodehandler".length());
                }
                
                ConfigurationHandlerData handlerData = new ConfigurationHandlerData();
                handlerData.setType(nodeHandlerType);
                handlerData.setModuleType(module.getModuleType());
                handlerData.setTitleTextId(configurationHandler.titleTextId());
                handlerData.setId(idCounter++);
                handlerData.setTypeName(typeName);
                
                this.configurationHandlers.put(typeName, handlerData);
                normalGroup.getHandlers().add(handlerData);
            }
        }
        
        if (!normalGroup.getHandlers().isEmpty()) {
            this.configurationGroups.add(normalGroup);
        }
        
        for (ConfigurationHandlerData handler : this.configurationHandlers.values()) {
            handler.setTitle(LanguageHelper.getFullTextId(handler.getModuleType(), handler.getTitleTextId()));
        }
        
        for (ConfigurationGroupData group : this.configurationGroups) {
            group.getHandlers().sort((h1, h2) -> h1.getTitle().compareTo(h2.getTitle()));
        }
    }
    
    @Override
    public boolean hasChildren() {
        return true;
    }
    
    @Override
    public JsonObject getViewData() {
        String path = getObjectId().isPresent() ? this.dataCache.get(NodeCache.class).getNode(getObjectId().getAsLong()).getFullPath() : null;
        
        JsonObject ret = new JsonObject();
        
        JsonArray groups = new JsonArray();
        
        for (ConfigurationGroupData g : this.configurationGroups) {
            JsonObject group = new JsonObject();
            group.addProperty("title", g.getTitle());
            JsonArray items = new JsonArray();
            g.getHandlers().forEach(h -> {
                JsonObject handler = new JsonObject();
                handler.addProperty("path", path != null ? (path + "/" + h.getTypeName()) : h.getTypeName());
                handler.addProperty("title", LanguageHelper.getFullTextId(h.getModuleType(), h.getTitleTextId()));
                items.add(handler);
            });
            group.add("items", items);
            groups.add(group);
        }
        
        ret.add("groups", groups);
        
        return ret;
    }
    
    @Override
    public ChildHandlerSettings getChildHandler(String path) {
        List<String> parts = new ArrayList<>();
        Arrays.stream(path.split("/")).map(p -> p.trim()).forEach(parts::add);
        if (parts.isEmpty()) {
            return null;
        }
        
        ConfigurationHandlerData handlerData = this.configurationHandlers.get(parts.get(0));
        if (handlerData == null) {
            return null;
        }
        
        NodeHandler handler = DependencyResolver.resolve(handlerData.getType());
        
        path = parts.get(0);
        parts.remove(0);
        
        handler.initialize(OptionalLong.empty(), null, path, getFullPath() + "/" + path, NodeAccessType.WRITE);
        
        ChildHandlerSettings ret = new ChildHandlerSettings();
        
        ret.setId(OptionalLong.of(handlerData.getId()));
        ret.setHandler(handler);
        ret.setTitle(LanguageHelper.getText(handlerData.getModuleType(), handlerData.getTitleTextId()));
        ret.setPath(path);
        ret.setRest(StringHelper.join(",", parts));
        
        return ret;
    }
}
