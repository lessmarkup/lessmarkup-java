package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.*;
import com.lessmarkup.interfaces.system.UserCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("prototype")
public class LoadUpdatesModel {

    private final DomainModelProvider domainModelProvider;
    private final DataCache dataCache;

    @Autowired
    public LoadUpdatesModel(DomainModelProvider domainModelProvider, DataCache dataCache){
        this.domainModelProvider = domainModelProvider;
        this.dataCache = dataCache;
    }

    public void handle(OptionalLong versionId, OptionalLong newVersionId, String path, JsonObject arguments, JsonObject returnValues, OptionalLong currentNodeId) {
        UserCache userCache = dataCache.get(UserCache.class);
        NodeCache nodeCache = dataCache.get(NodeCache.class);

        if ((!newVersionId.isPresent() || newVersionId == versionId) && !currentNodeId.isPresent()) {
            return;
        }

        List<Tuple<Long, NotificationProvider>> handlers = new ArrayList<>();

        NotificationProvider currentProvider = null;

        for (Tuple<CachedNodeInformation, NodeAccessType> node : userCache.getNodes()) {
            if (!node.getValue1().getHandlerType().isAssignableFrom(NotificationProvider.class)) {
                continue;
            }
            NodeHandler handler = DependencyResolver.resolve(node.getValue1().getHandlerType());
            NotificationProvider notificationProvider = (NotificationProvider) handler;
            if (notificationProvider == null) {
                continue;
            }

            if (currentNodeId.isPresent() && currentProvider == null && node.getValue1().getNodeId() == currentNodeId.getAsLong()) {
                currentProvider = notificationProvider;
            }

            JsonObject settings = null;
            if (node.getValue1().getSettings() != null && node.getValue1().getSettings().length() > 0) {
                settings = JsonSerializer.deserialize(node.getValue1().getSettings());
            }

            handler.initialize(OptionalLong.of(node.getValue1().getNodeId()), settings, node.getValue1().getPath(), node.getValue1().getFullPath(), node.getValue2());
            handlers.add(new Tuple(node.getValue1().getNodeId(), notificationProvider));
        }

        if ((!newVersionId.isPresent() || newVersionId == versionId) && currentProvider == null) {
            return;
        }

        JsonArray notificationChanges = new JsonArray();

        try (DomainModel domainModel = domainModelProvider.create()) {
            for (Tuple<Long, NotificationProvider> handler : handlers) {
                if (handler.getValue2() == null) {
                    continue;
                }

                if (handler.getValue2() == currentProvider && currentProvider != null) {
                    int change = handler.getValue2().getValueChange(null, newVersionId, domainModel);
                    JsonObject c = new JsonObject();
                    c.addProperty("id", handler.getValue1());
                    c.addProperty("newValue", change);
                    notificationChanges.add(c);
                }
                else {
                    int change = handler.getValue2().getValueChange(versionId, newVersionId, domainModel);

                    if (change > 0) {
                        JsonObject c = new JsonObject();
                        c.addProperty("id", handler.getValue1());
                        c.addProperty("change", change);
                        notificationChanges.add(c);
                    }
                }
            }

            NodeHandler currentHandler = nodeCache.getNodeHandler(path);

            if (currentHandler != null && newVersionId.isPresent()) {
                JsonObject updates = new JsonObject();
                currentHandler.processUpdates(versionId, newVersionId.getAsLong(), updates, domainModel, arguments);
                if (updates.entrySet().size() > 0) {
                    returnValues.add("updates", updates);
                }
            }
        }

        if (notificationChanges.size() > 0) {
            returnValues.add("notificationChanges", notificationChanges);
        }
    }
}
