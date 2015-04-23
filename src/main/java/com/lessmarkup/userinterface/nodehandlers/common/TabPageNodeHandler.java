package com.lessmarkup.userinterface.nodehandlers.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.structure.*;
import com.lessmarkup.userinterface.model.structure.LoadNodeViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public abstract class TabPageNodeHandler extends AbstractNodeHandler {
    static class TabPage {
        private String title;
        private Class<? extends NodeHandler> type;
        private JsonObject viewData;
        private String viewBody;
        private String path;
        private String fullPath;
        private OptionalLong pageId;
        private String settings;
        private Optional<NodeAccessType> accessType;
        private String uniqueId;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Class<? extends NodeHandler> getType() {
            return type;
        }

        public void setType(Class<? extends NodeHandler> type) {
            this.type = type;
        }

        public JsonObject getViewData() {
            return viewData;
        }

        public void setViewData(JsonObject viewData) {
            this.viewData = viewData;
        }

        public String getViewBody() {
            return viewBody;
        }

        public void setViewBody(String viewBody) {
            this.viewBody = viewBody;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getFullPath() {
            return fullPath;
        }

        public void setFullPath(String fullPath) {
            this.fullPath = fullPath;
        }

        public OptionalLong getPageId() {
            return pageId;
        }

        public void setPageId(OptionalLong pageId) {
            this.pageId = pageId;
        }

        public String getSettings() {
            return settings;
        }

        public void setSettings(String settings) {
            this.settings = settings;
        }

        public Optional<NodeAccessType> getAccessType() {
            return accessType;
        }

        public void setAccessType(Optional<NodeAccessType> accessType) {
            this.accessType = accessType;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }
    }

    private final List<TabPage> pages = new ArrayList<>();
    private final DataCache dataCache;
    private final CurrentUser currentUser;
    private final List<String> scripts = new ArrayList<>();

    protected TabPageNodeHandler(DataCache dataCache, CurrentUser currentUser) {
        this.currentUser = currentUser;
        this.dataCache = dataCache;
    }

    protected <T extends NodeHandler> void addPage(Class<T> type, String title, String path) {
        TabPage page = new TabPage();
        page.setType(type);
        page.setPath(path);
        page.setTitle(title);
        pages.add(page);
    }

    protected <T extends NodeHandler> void addPage(Class<T> type, String title, String path, OptionalLong pageId) {
        TabPage page = new TabPage();
        page.setType(type);
        page.setPath(path);
        page.setTitle(title);
        page.setPageId(pageId);
        pages.add(page);
    }

    @Override
    protected Object initialize() {
        NodeCache nodeCache = dataCache.get(NodeCache.class);

        String parentPath = "";

        if (getObjectId().isPresent()) {
            CachedNodeInformation currentNode = nodeCache.getNode(getObjectId().getAsLong());
            parentPath = currentNode.getFullPath();

            for (CachedNodeInformation child : currentNode.getChildren()) {
                NodeAccessType accessType = child.checkRights(currentUser);

                if (accessType == NodeAccessType.NO_ACCESS) {
                    continue;
                }

                TabPage page = new TabPage();
                page.setType(child.getHandlerType());
                page.setPageId(OptionalLong.of(child.getNodeId()));
                page.setSettings(child.getSettings());
                page.setPath(child.getPath());
                page.setFullPath(child.getFullPath());
                page.setTitle(child.getTitle());
                page.setAccessType(Optional.of(accessType));

                pages.add(page);
            }
        }

        pages.sort((p1, p2) -> p1.getTitle().compareTo(p2.getTitle()));

        int unknownPath = 1;
        int uniqueId = 1;

        for (TabPage page : pages) {
            NodeHandler handler = createChildHandler(page.getType());

            JsonElement nodeSettings = null;

            if (handler.getSettingsModel() != null && !StringHelper.isNullOrEmpty(page.getSettings())) {
                nodeSettings = JsonSerializer.deserializeToTree(page.getSettings());
            }

            if (StringHelper.isNullOrWhitespace(page.getPath())) {
                page.setPath(String.format("page_%s", unknownPath++));
                page.setFullPath(page.getPath());

                if (!StringHelper.isNullOrEmpty(parentPath)) {
                    page.setFullPath(parentPath + "/" + page.getPath());
                }
            }

            handler.initialize(page.getPageId(), 
                    nodeSettings != null && nodeSettings.isJsonObject() ? nodeSettings.getAsJsonObject() : null, 
                    page.getPath(), page.getFullPath(), page.getAccessType().isPresent() ? page.getAccessType().get() : getAccessType());

            page.setViewData(handler.getViewData());
            page.setViewBody(LoadNodeViewModel.getViewTemplate(handler, dataCache));
            page.setUniqueId(String.format("page_%s", uniqueId++));

            List<String> handlerScripts = handler.getScripts();
            if (handlerScripts != null) {
                this.scripts.addAll(handlerScripts);
            }
        }

        pages.removeIf(p -> p.getViewBody() == null);

        return null;
    }

    @Override
    public JsonObject getViewData() {
        JsonObject ret = new JsonObject();
        JsonArray pagesArray = new JsonArray();
        this.pages.forEach(p -> {
            JsonObject o = new JsonObject();
            o.addProperty("pageId", p.getPageId().orElse(0));
            o.addProperty("path", p.getPath());
            o.addProperty("title", p.getTitle());
            o.addProperty("viewBody", p.getViewBody());
            o.add("viewData", p.getViewData());
            o.addProperty("uniqueId", p.getUniqueId());
            pagesArray.add(o);
        });
        ret.add("pages", pagesArray);
        JsonArray requires = new JsonArray();
        this.scripts.forEach(s -> requires.add(new JsonPrimitive(s)));
        ret.add("requires", requires);
        return ret;
    }

    @Override
    public String getViewType() {
        return "TabPage";
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public ChildHandlerSettings getChildHandler(String path) {
        Optional<TabPage> page = pages.stream().filter(p -> p.getPath().equals(path)).findFirst();
        if (!page.isPresent()) {
            return null;
        }
        NodeHandler handler = createChildHandler(page.get().getType());

        JsonElement nodeSettings = null;
        if (handler.getSettingsModel() != null && !StringHelper.isNullOrEmpty(page.get().getSettings())) {
            nodeSettings = JsonSerializer.deserializeToTree(page.get().getSettings());
        }

        handler.initialize(page.get().getPageId(), 
                nodeSettings != null && nodeSettings.isJsonObject() ? nodeSettings.getAsJsonObject() : null, 
                page.get().getPath(), page.get().getFullPath(), page.get().getAccessType().isPresent() ? page.get().getAccessType().get() : getAccessType());

        ChildHandlerSettings ret = new ChildHandlerSettings();
        ret.setHandler(handler);
        ret.setPath(path);
        ret.setTitle(page.get().getTitle());
        ret.setId(page.get().getPageId());

        return ret;
    }
}
