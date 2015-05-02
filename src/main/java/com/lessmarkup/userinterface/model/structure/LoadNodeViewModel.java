package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.structure.NodeCache;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.system.ResourceCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

@Component
@Scope("prototype")
public class LoadNodeViewModel {
    
    private NodeHandler nodeHandler;
    private final DataCache dataCache;
    private OptionalLong nodeId = OptionalLong.empty();
    private List<NodeBreadcrumbModel> breadcrumbs;
    private List<ToolbarButtonModel> toolbarButtons;
    private String title;
    private String path;
    private String templateId;
    private String template;
    private JsonObject viewData;
    private boolean isStatic;
    private List<String> require;

    @Autowired
    public LoadNodeViewModel(DataCache dataCache) {
        this.dataCache = dataCache;
    }
    
    public NodeHandler getNodeHandler() {
        return this.nodeHandler;
    }
    
    public OptionalLong getNodeId() {
        return this.nodeId;
    }
    
    public boolean initialize(String path, List<String> cachedTemplates, boolean initializeUiElements, boolean tryCreateResult) {
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch(UnsupportedEncodingException e) {
        }
        
        if (path != null) {
            int queryPost = path.indexOf('?');
            if (queryPost >= 0) {
                path = path.substring(0, queryPost);
            }
        }
        
        NodeCache nodeCache = this.dataCache.get(NodeCache.class);
        
        if (initializeUiElements) {
            this.breadcrumbs = new ArrayList<>();
        }
        
        this.nodeHandler = nodeCache.getNodeHandler(path, (handler, nodeTitle, nodePath, nodeRest, nodeIdLocal) -> {
            if (nodeIdLocal.isPresent()) {
                this.nodeId = nodeIdLocal;
            }
            
            if (initializeUiElements) {
                this.breadcrumbs.add(new NodeBreadcrumbModel(nodeTitle, nodePath));
            }
            
            this.title = nodeTitle;
            this.path = nodePath;
            
            return false;
        });

        if (initializeUiElements && this.breadcrumbs.size() > 0) {
            this.breadcrumbs.remove(this.breadcrumbs.size()-1);
        }
        
        if (this.nodeHandler == null) {
            return false;
        }
        
        this.templateId = this.nodeHandler.getTemplateId();
        
        if (initializeUiElements) {
            this.toolbarButtons = new ArrayList<>();
            
            if (cachedTemplates == null || !cachedTemplates.stream().anyMatch(t -> t.equals(this.templateId))) {
                this.template = getViewTemplate(this.nodeHandler, this.dataCache);
                if (this.template == null) {
                    return false;
                }
            }
            
            this.viewData = nodeHandler.getViewData();
        }
        
        this.isStatic = nodeHandler.isStatic();
        this.require = nodeHandler.getScripts();
        
        return true;
    }
    
    public JsonObject toJson() {
        JsonObject ret = new JsonObject();
        
        ret.addProperty("template", this.template);
        ret.addProperty("templateId", this.templateId);
        ret.addProperty("title", this.title);
        ret.add("viewData", this.viewData);
        ret.addProperty("isStatic", this.isStatic);
        ret.addProperty("path", this.path);
        
        JsonArray array = new JsonArray();
        
        if (this.require != null) {
            for (String script : this.require) {
                array.add(new JsonPrimitive(script));
            }
        }
        
        ret.add("require", array);
        
        if (this.nodeId.isPresent()) {
            ret.addProperty("nodeId", this.nodeId.getAsLong());
        } else {
            ret.add("nodeId", JsonNull.INSTANCE);
        }
        
        final JsonArray arrayBreadcrumbs = new JsonArray();
        
        if (this.breadcrumbs != null) {
            this.breadcrumbs.stream().forEach(b -> {
                JsonObject b1 = new JsonObject();
                b1.addProperty("text", b.getText());
                b1.addProperty("url", b.getUrl());
                arrayBreadcrumbs.add(b1);
            });
        }
        
        ret.add("breadcrumbs", arrayBreadcrumbs);
        
        JsonArray arrayButtons = new JsonArray();
        
        if (this.toolbarButtons != null) {

            for (ToolbarButtonModel b : toolbarButtons) {
                JsonObject b1 = new JsonObject();
                b1.addProperty("id", b.getId());
                b1.addProperty("text", b.getText());
                arrayButtons.add(b1);
            }
        }
        
        ret.add("toolbarButtons", arrayButtons);
        
        return ret;
    }
    
    public static String getViewTemplate(NodeHandler handler, DataCache dataCache) {
        String viewPath = getViewPath(handler.getViewType());
        ResourceCache resourceCache = dataCache.get(ResourceCache.class);
        String template = resourceCache.parseText(viewPath + ".html");
        List<String> stylesheets = handler.getStylesheets();
        if (stylesheets != null && stylesheets.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("<style scoped=\"scoped\">");
            stylesheets.forEach(stylesheet -> {
                sb.append(resourceCache.readText(stylesheet + ".css"));
            });
            sb.append("</style>");
            template = sb.toString() + template;
        }
        return template;
    }
    
    public static String getViewPath(String viewName) {
        if (viewName.endsWith("NodeHandler")) {
            viewName = viewName.substring(0, viewName.length()-"NodeHandler".length());
        }
        
        return "views/" + StringHelper.toJsonCase(viewName);
    }
}
