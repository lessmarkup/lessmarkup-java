package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.*;
import com.lessmarkup.interfaces.system.UserCache;

import java.util.*;

public class LoadUpdatesModel {

    private final DomainModelProvider domainModelProvider;
    private final DataCache dataCache;

    @Inject
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

        for (scala.Tuple2<CachedNodeInformation, NodeAccessType> node : userCache.getNodesJava()) {
            if (!node._1().getHandlerType().isAssignableFrom(NotificationProvider.class)) {
                continue;
            }
            NodeHandler handler = DependencyResolver.resolve(node._1().getHandlerType());
            NotificationProvider notificationProvider = (NotificationProvider) handler;
            if (notificationProvider == null) {
                continue;
            }

            if (currentNodeId.isPresent() && currentProvider == null && node._1().getNodeId() == currentNodeId.getAsLong()) {
                currentProvider = notificationProvider;
            }

            JsonElement settings = null;
            if (node._1().getSettings() != null && node._1().getSettings().length() > 0) {
                settings = JsonSerializer.deserializeToTree(node._1().getSettings());
            }

            handler.initialize(OptionalLong.of(node._1().getNodeId()),
                    settings != null && settings.isJsonObject() ? settings.getAsJsonObject() : null, node._1().getPath(), node._1().getFullPath(), node._2());
            handlers.add(new Tuple<>(node._1().getNodeId(), notificationProvider));
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

                if (handler.getValue2() == currentProvider) {
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
