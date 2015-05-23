package com.lessmarkup.userinterface.nodehandlers.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.recordmodel.RecordModelCache;
import com.lessmarkup.interfaces.structure.ChildHandlerSettings;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.Tuple;
import com.lessmarkup.userinterface.model.configuration.NodeSettingsModel;

import java.util.OptionalLong;

@ConfigurationHandler(titleTextId = TextIds.VIEWS_TREE)
public class NodeListNodeHandler extends AbstractNodeHandler {
    public static String getHandlerName(Class<?> handlerType, String moduleType) {
        String typeName = handlerType.getName();
        if (typeName.endsWith("NodeHandler")) {
            typeName = typeName.substring(0, typeName.length() - "NodeHandler".length());
        }
        return typeName + " / " + moduleType;
    }

    public static class LayoutInfo {
        private long nodeId;
        private int level;

        public long getNodeId() {
            return nodeId;
        }

        public void setNodeId(long nodeId) {
            this.nodeId = nodeId;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }

    private final ModuleProvider moduleProvider;
    private final DataCache dataCache;

    @Inject
    public NodeListNodeHandler(ModuleProvider moduleProvider, DataCache dataCache) {
        this.moduleProvider = moduleProvider;
        this.dataCache = dataCache;
        addScript("scripts/controllers/NodeListController");
    }

    @Override
    public JsonObject getViewData() {
        RecordModelCache modelCache = dataCache.get(RecordModelCache.class);

        NodeSettingsModel node = DependencyResolver.resolve(NodeSettingsModel.class);

        JsonObject ret = new JsonObject();

        NodeSettingsModel rootNode = node.getRootNode();

        ret.add("root", rootNode != null ? JsonSerializer.serializePojoToTree(node.getRootNode()) : JsonNull.INSTANCE);
        ret.addProperty("nodeSettingsModelId", modelCache.getDefinition(NodeSettingsModel.class).getId());

        JsonArray handlers = new JsonArray();
        moduleProvider.getNodeHandlers().forEach(id -> {
            JsonObject o = new JsonObject();
            o.addProperty("id", id);
            Tuple<Class<? extends NodeHandler>, String> handler = moduleProvider.getNodeHandler(id);
            o.addProperty("name", getHandlerName(handler.getValue1(), handler.getValue2()));
            handlers.add(o);
        });
        ret.add("nodeHandlers", handlers);
        return ret;
    }

    public JsonElement updateParent(long nodeId, OptionalLong parentId, int order) {
        NodeSettingsModel node = DependencyResolver.resolve(NodeSettingsModel.class);
        node.setNodeId(nodeId);
        node.setParentId(parentId);
        node.setPosition(order);
        return node.updateParent();
    }

    public Object createNode(NodeSettingsModel node) {
        return node.createNode();
    }

    public Object deleteNode(long id) {
        NodeSettingsModel node = DependencyResolver.resolve(NodeSettingsModel.class);
        node.setNodeId(id);
        return node.deleteNode();
    }

    public Object updateNode(NodeSettingsModel node) {
        return node.updateNode();
    }

    public Object changeSettings(long nodeId, JsonElement settings) {
        NodeSettingsModel node = DependencyResolver.resolve(NodeSettingsModel.class);
        node.setSettings(settings);
        node.setNodeId(nodeId);
        return node.changeSettings();
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public ChildHandlerSettings getChildHandler(String path) {
        String[] split = path.split("/");
        long nodeId;
        if (split.length != 2 || !"access".equals(split[1])) {
            return null;
        }
        try {
            nodeId = Long.parseLong(split[0]);
        } catch (Exception e) {
            return null;
        }
        NodeAccessNodeHandler handler = DependencyResolver.resolve(NodeAccessNodeHandler.class);
        handler.initialize(nodeId);
        handler.initialize(OptionalLong.of(nodeId), null, path, getFullPath() + "/" + path, getAccessType());

        ChildHandlerSettings ret = new ChildHandlerSettings();
        ret.setHandler(handler);
        ret.setPath(path);
        ret.setTitle(LanguageHelper.getFullTextId(Constants.ModuleTypeMain(), TextIds.NODE_ACCESS));
        ret.setId(OptionalLong.of(nodeId));
        return ret;
    }
}
