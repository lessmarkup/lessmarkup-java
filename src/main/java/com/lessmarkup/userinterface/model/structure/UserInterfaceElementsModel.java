package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.CachedNodeInformation;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import com.lessmarkup.interfaces.structure.NodeCache;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.NotificationProvider;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.OptionalLong;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Scope("prototype")
public class UserInterfaceElementsModel {
    private final DomainModelProvider domainModelProvider;
    private final DataCache dataCache;

    @Autowired
    public UserInterfaceElementsModel(DomainModelProvider domainModelProvider, DataCache dataCache) {
        this.domainModelProvider = domainModelProvider;
        this.dataCache = dataCache;
    }
    
    public void handle(JsonObject serverConfiguration, OptionalLong lastChangeId) {
        JsonArray notifications = new JsonArray();
        NodeCache nodeCache = this.dataCache.get(NodeCache.class);
        SiteConfiguration siteConfiguration = this.dataCache.get(SiteConfiguration.class);
        
        try (DomainModel domainModel = this.domainModelProvider.create()) {
            for (CachedNodeInformation nodeInfo : nodeCache.getNodes()) {
                Class<? extends NodeHandler> handlerType = nodeInfo.getHandlerType();
                if (handlerType == null || !NotificationProvider.class.isAssignableFrom(handlerType)) {
                    continue;
                }
                
                NodeAccessType accessType = nodeInfo.checkRights(RequestContextHolder.getContext().getCurrentUser());
                
                if (accessType == NodeAccessType.NO_ACCESS) {
                    return;
                }
                
                NodeHandler node = DependencyResolver.resolve(nodeInfo.getHandlerType());
                
                JsonElement settings = null;
                
                if (nodeInfo.getSettings() != null && nodeInfo.getSettings().length() > 0) {
                    settings = JsonSerializer.deserializeToTree(nodeInfo.getSettings());
                }
                
                node.initialize(OptionalLong.of(nodeInfo.getNodeId()), 
                        settings != null && settings.isJsonObject() ? settings.getAsJsonObject() : null, 
                        nodeInfo.getPath(), nodeInfo.getFullPath(), accessType);
                
                NotificationProvider notificationProvider = (NotificationProvider) node;
                
                JsonObject notification = new JsonObject();
                
                notification.addProperty("id", nodeInfo.getNodeId());
                notification.addProperty("title", notificationProvider.getTitle());
                notification.addProperty("tooltip", notificationProvider.getTooltip());
                notification.addProperty("icon", notificationProvider.getIcon());
                notification.addProperty("path", nodeInfo.getFullPath());
                notification.addProperty("count", notificationProvider.getValueChange(OptionalLong.empty(), lastChangeId, domainModel));
                notifications.add(notification);
            }
        } catch (Exception ex) {
            Logger.getLogger(UserInterfaceElementsModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JsonArray menuNodes = new JsonArray();
        
        nodeCache.getNodes().stream().filter(n -> n.isAddToMenu() && n.isVisible()).forEach(n -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("title", n.getTitle());
            obj.addProperty("url", n.getFullPath());
            menuNodes.add(obj);
        });
        
        serverConfiguration.add("topMenu", menuNodes);
        serverConfiguration.add("collections", notifications);
        
        if (siteConfiguration.getHasNavigationBar()) {
            JsonArray navigationTree = new JsonArray();
            fillNavigationBarItems(nodeCache.getRootNode().getChildren(), 0, menuNodes);
            serverConfiguration.add("navigationTree", navigationTree);
        }
    }
    
    private void fillNavigationBarItems(List<CachedNodeInformation> nodes, int level, JsonArray menuItems) {
        nodes.forEach(node -> {
            if (!node.isVisible()) {
                return;
            }
            
            NodeAccessType accessType = node.checkRights(RequestContextHolder.getContext().getCurrentUser());
            
            if (accessType == NodeAccessType.NO_ACCESS) {
                return;
            }
            
            JsonObject model = new JsonObject();
            
            model.addProperty("title", node.getTitle());
            model.addProperty("url", node.getFullPath());
            model.addProperty("level", level);
            
            menuItems.add(model);
            
            fillNavigationBarItems(node.getChildren(), level + 1, menuItems);
        });
    }
}
