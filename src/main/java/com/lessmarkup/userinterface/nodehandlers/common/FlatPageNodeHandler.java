package com.lessmarkup.userinterface.nodehandlers.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.structure.*;
import com.lessmarkup.userinterface.model.common.FlatPagePosition;
import com.lessmarkup.userinterface.model.common.FlatPageSettingsModel;
import com.lessmarkup.userinterface.model.structure.LoadNodeViewModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public abstract class FlatPageNodeHandler extends AbstractNodeHandler {

    class FlatNodeEntry {
        private String title;
        private JsonObject viewData;
        private String viewBody;
        private Class<? extends NodeHandler> handlerType;
        private long nodeId;
        private String settings;
        private String anchor;
        private String uniqueId;
        private int level;
        private String path;
        private String fullPath;
        private NodeAccessType accessType;
        private CachedNodeInformation source;

        public void setTitle(String title) {
            this.title = title;
        }
        public String getTitle() {
            return title;
        }

        public void setViewData(JsonObject viewData) {
            this.viewData = viewData;
        }
        public JsonObject getViewData() {
            return viewData;
        }

        public void setViewBody(String viewBody) {
            this.viewBody = viewBody;
        }
        public String getViewBody() {
            return viewBody;
        }

        public void setHandlerType(Class<? extends NodeHandler> handlerType) {
            this.handlerType = handlerType;
        }
        public Class<? extends NodeHandler> getHandlerType() { return handlerType; }

        public void setNodeId(long nodeId) {
            this.nodeId = nodeId;
        }
        public long getNodeId() { return nodeId; }

        public void setSettings(String settings) {
            this.settings = settings;
        }
        public String getSettings() {
            return settings;
        }

        public void setAnchor(String anchor) {
            this.anchor = anchor;
        }
        public String getAnchor() {
            return anchor;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }
        public String getUniqueId() {
            return uniqueId;
        }

        public void setLevel(int level) {
            this.level = level;
        }
        public int getLevel() {
            return level;
        }

        public void setPath(String path) {
            this.path = path;
        }
        public String getPath() {
            return path;
        }

        public void setFullPath(String fullPath) {
            this.fullPath = fullPath;
        }
        public String getFullPath() {
            return fullPath;
        }

        public void setAccessType(NodeAccessType accessType) {
            this.accessType = accessType;
        }
        public NodeAccessType getAccessType() {
            return accessType;
        }

        public void setSource(CachedNodeInformation source) {
            this.source = source;
        }
        public CachedNodeInformation getSource() {
            return source;
        }
    }

    class TreeNodeEntry {
        private String anchor;
        private String title;
        private final List<TreeNodeEntry> children = new ArrayList<>();

        public void setAnchor(String anchor) {
            this.anchor = anchor;
        }
        public String getAnchor() {
            return anchor;
        }

        public void setTitle(String title) {
            this.title = title;
        }
        public String getTitle() {
            return title;
        }

        public List<TreeNodeEntry> getChildren() {
            return children;
        }
    }

    private final List<FlatNodeEntry> flatNodeList = new ArrayList<>();
    private TreeNodeEntry treeRoot;
    private final List<String> scripts = new ArrayList<>();

    private final DataCache dataCache;
    private final CurrentUser currentUser;

    protected FlatPageNodeHandler(DataCache dataCache, CurrentUser currentUser) {
        this.dataCache = dataCache;
        this.currentUser = currentUser;
    }

    private void fillFlatAndTreeList(CachedNodeInformation parent, List<FlatNodeEntry> nodes, TreeNodeEntry parentTreeNode, String anchor, int level, int maxLevel) {
        for (CachedNodeInformation child : parent.getChildren()) {
            if (child.getHandlerType() == null) {
                continue;
            }

            if (child.getHandlerType().equals(FlatPageNodeHandler.class) || !child.isVisible()) {
                continue;
            }

            NodeAccessType accessType = child.checkRights(currentUser);

            if (accessType == NodeAccessType.NO_ACCESS) {
                continue;
            }

            String childAnchor = StringHelper.isNullOrEmpty(anchor) ? "" : anchor + "_";
            FlatNodeEntry entry = new FlatNodeEntry();
            entry.setTitle(child.getTitle());
            entry.setHandlerType(child.getHandlerType());
            entry.setNodeId(child.getNodeId());
            entry.setSettings(child.getSettings());
            entry.setAnchor(childAnchor + child.getPath());
            entry.setLevel(level);
            entry.setPath(child.getFullPath());
            entry.setFullPath(child.getFullPath());
            entry.setSource(child);
            entry.setAccessType(accessType);

            TreeNodeEntry treeNode = new TreeNodeEntry();
            treeNode.setTitle(entry.getTitle());
            treeNode.setAnchor(entry.getAnchor());

            parentTreeNode.getChildren().add(treeNode);

            nodes.add(entry);

            if (level < maxLevel) {
                fillFlatAndTreeList(child, nodes, treeNode, entry.getAnchor(), level + 1, maxLevel);
            }
        }
    }

    @Override
    protected Object initialize() {
        FlatPageSettingsModel settingsModel = getSettings(FlatPageSettingsModel.class);

        NodeCache nodeCache = dataCache.get(NodeCache.class);

        treeRoot = new TreeNodeEntry();

        if (getObjectId().isPresent()) {
            CachedNodeInformation currentNode = nodeCache.getNode(getObjectId().getAsLong());
            fillFlatAndTreeList(currentNode, flatNodeList, treeRoot, "", 1, (settingsModel != null && settingsModel.getLevelToLoad() != 0) ? settingsModel.getLevelToLoad() : 2);
        }

        if (settingsModel == null || settingsModel.isLoadOnShow()) {
            for (FlatNodeEntry node : flatNodeList) {
                NodeHandler handler = createChildHandler(node.getHandlerType());
                JsonElement nodeSettings = null;

                for (String script : handler.getScripts()) {
                    addScript(script);
                }

                for (String stylesheet : handler.getStylesheets()) {
                    addStylesheet(stylesheet);
                }

                if (handler.getSettingsModel() != null && !StringHelper.isNullOrEmpty(node.getSettings())) {
                    nodeSettings = JsonSerializer.deserializeToTree(node.getSettings());
                }

                handler.initialize(OptionalLong.of(node.getNodeId()), 
                        nodeSettings != null && nodeSettings.isJsonObject() ? nodeSettings.getAsJsonObject() : null, 
                        node.getPath(), node.getFullPath(), node.getAccessType());

                node.setViewData(handler.getViewData());
                node.setViewBody(LoadNodeViewModel.getViewTemplate(handler, dataCache));

                List<String> handlerScripts = handler.getScripts();

                if (handlerScripts != null) {
                    handlerScripts.addAll(handlerScripts);
                }
            }

            flatNodeList.removeIf(n -> n.getViewBody() == null);
        }

        int pageIndex = 1;

        for (FlatNodeEntry node : flatNodeList) {
            node.setUniqueId(String.format("flatpage%d", pageIndex++));
        }

        return null;
    }

    @Override
    public JsonObject getViewData() {
        FlatPageSettingsModel settingsModel = getSettings(FlatPageSettingsModel.class);

        JsonObject ret = new JsonObject();
        
        JsonArray children = new JsonArray();
        for (TreeNodeEntry child : treeRoot.getChildren()) {
            children.add(JsonSerializer.serializePojoToTree(child));
        }

        ret.add("tree", children);

        JsonArray flatArray = new JsonArray();
        for (FlatNodeEntry entry : flatNodeList) {
            JsonObject o = new JsonObject();
            o.addProperty("anchor", entry.getAnchor());
            o.addProperty("level", entry.getLevel());
            o.addProperty("nodeId", entry.getNodeId());
            o.addProperty("path", entry.getPath());
            o.addProperty("title", entry.getTitle());
            o.addProperty("uniqueId", entry.getUniqueId());
            o.addProperty("viewBody", entry.getViewBody());
            o.add("viewData", entry.getViewData());
            flatArray.add(o);
        }

        ret.add("flat", flatArray);
        ret.addProperty("position", settingsModel != null ? settingsModel.getPosition().toString() : FlatPagePosition.RIGHT.toString());
        JsonArray scriptsArray = new JsonArray();
        scripts.forEach(s -> scriptsArray.add(new JsonPrimitive(s)));
        ret.add("scripts", scriptsArray);

        return ret;
    }

    @Override
    public Class<?> getSettingsModel() {
        return FlatPageSettingsModel.class;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    private NodeHandler constructHandler(FlatNodeEntry node) {
        NodeAccessType accessType = node.getSource().checkRights(currentUser);

        if (accessType == NodeAccessType.NO_ACCESS) {
            return null;
        }

        NodeHandler handler = DependencyResolver.resolve(node.getHandlerType());

        JsonElement nodeSettings = null;

        if (handler.getSettingsModel() != null && !StringHelper.isNullOrEmpty(node.getSettings())) {
            nodeSettings = JsonSerializer.deserializeToTree(node.getSettings());
        }

        handler.initialize(OptionalLong.of(node.getNodeId()), 
                nodeSettings != null && nodeSettings.isJsonObject() ? nodeSettings.getAsJsonObject() : null,
                node.getPath(), node.getFullPath(), accessType);

        return handler;
    }

    @Override
    public ChildHandlerSettings getChildHandler(String path) {
        Optional<FlatNodeEntry> node = flatNodeList.stream().filter(n -> n.getPath().equals(path)).findFirst();

        if (!node.isPresent()) {
            return null;
        }

        NodeHandler handler = constructHandler(node.get());

        ChildHandlerSettings settings = new ChildHandlerSettings();
        settings.setHandler(handler);
        settings.setId(OptionalLong.of(node.get().getNodeId()));
        settings.setPath(path);
        settings.setTitle(node.get().getTitle());

        return settings;
    }

    @Override
    public Tuple<Object, Method> getActionHandler(String name, JsonObject data) {
        JsonElement nodeIdElement = data.get("flatNodeId");
        if (nodeIdElement != null) {
            long nodeId = nodeIdElement.getAsLong();
            Optional<FlatNodeEntry> node = flatNodeList.stream().filter(n -> n.getNodeId() == nodeId).findFirst();
            if (node.isPresent()) {
                NodeHandler handler = constructHandler(node.get());
                return handler.getActionHandler(name, data);
            }
        }

        return super.getActionHandler(name, data);
    }
}
