package com.lessmarkup.userinterface.model.configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.Node;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.exceptions.RecordValidationException;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.recordmodel.EnumSource;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.InputSource;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.recordmodel.RecordModelCache;
import com.lessmarkup.interfaces.recordmodel.RecordModelDefinition;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.Tuple;
import com.lessmarkup.userinterface.nodehandlers.configuration.NodeListNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.logging.Level;

@Component
@Scope("prototype")
public class NodeSettingsModel extends RecordModel<NodeSettingsModel> implements InputSource {

    private final ModuleProvider moduleProvider;
    private final DomainModelProvider domainModelProvider;
    private final DataCache dataCache;
    private final ChangeTracker changeTracker;
    
    private String title;
    private String handlerId;
    private Object settings;
    private String settingsModelId;
    private long nodeId;
    private boolean customizable;
    private boolean enabled;
    private boolean addToMenu;
    private String path;
    private int position;
    private String roleText;
    private final List<NodeSettingsModel> children = new ArrayList<>();
    private OptionalLong parentId;

    @Autowired
    public NodeSettingsModel(ModuleProvider moduleProvider, DomainModelProvider domainModelProvider, DataCache dataCache, ChangeTracker changeTracker) {
        super(TextIds.NODE_SETTINGS);
        this.moduleProvider = moduleProvider;
        this.domainModelProvider = domainModelProvider;
        this.dataCache = dataCache;
        this.changeTracker = changeTracker;
    }
    
    @InputField(type = InputFieldType.TEXT, textId = TextIds.TITLE, required = true)
    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    @InputField(type = InputFieldType.SELECT, textId = TextIds.HANDLER, required = true)
    public void setHandlerId(String handlerId) { this.handlerId = handlerId; }
    public String getHandlerId() { return handlerId; }

    public void setSettings(Object settings) { this.settings = settings; }
    public Object getSettings() { return settings; }

    public void setSettingsModelId(String settingsModelId) { this.settingsModelId = settingsModelId; }
    public String getSettingsModelId() { return settingsModelId; }

    public void setNodeId(long nodeId) { this.nodeId = nodeId; }
    public long getNodeId() { return nodeId; }

    public void setCustomizable(boolean customizable) { this.customizable = customizable; }
    public boolean isCustomizable() { return customizable; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.ENABLED, defaultValue = "true")
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.ADD_TO_MENU, defaultValue = "false")
    public void setAddToMenu(boolean addToMenu) { this.addToMenu = addToMenu; }
    public boolean isAddToMenu() { return addToMenu; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.PATH, required = true)
    public void setPath(String path) { this.path = path; }
    public String getPath() { return path; }

    public void setPosition(int order) { this.position = order; }
    public int getPosition() { return position; }

    public void setRoleText(String roleText) { this.roleText = roleText; }
    public String getRoleText() { return roleText; }

    public List<NodeSettingsModel> getChildren() { return children; }

    public void setParentId(OptionalLong parentId) { this.parentId = parentId; }
    public OptionalLong getParentId() { return parentId; }

    @Override
    public List<EnumSource> getEnumValues(String fieldName) {
        switch (StringHelper.toJsonCase(fieldName)) {
            case "handlerId": {
                List<String> modules = new ArrayList<>();
                moduleProvider.getModules().stream().map(ModuleConfiguration::getModuleType).forEach(modules::add);
                List<EnumSource> ret = new ArrayList<>();
                moduleProvider.getNodeHandlers().forEach(id -> {
                    Tuple<Class<? extends NodeHandler>, String> handler = moduleProvider.getNodeHandler(id);
                    if (!modules.contains(handler.getValue2())) {
                        return;
                    }
                    EnumSource enumSource = new EnumSource(id, NodeListNodeHandler.getHandlerName(handler.getValue1(), handler.getValue2()));
                    ret.add(enumSource);
                });
                return ret;
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    public Object createNode() {
        RecordModelCache modelCache = dataCache.get(RecordModelCache.class);
        RecordModelDefinition definition = modelCache.getDefinition(NodeSettingsModel.class);
        try {
            definition.validateInput(JsonSerializer.serializeToTree(this), true, null);
        } catch (RecordValidationException ex) {
            LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, ex);
            return null;
        }

        Node target = new Node();
        target.setEnabled(enabled);
        target.setHandlerId(handlerId);
        target.setParentId(parentId);
        target.setPosition(position);
        target.setPath(path);
        target.setAddToMenu(addToMenu);
        target.setSettings(settings != null ? JsonSerializer.serialize(settings) : null);
        target.setTitle(title);

        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            domainModel.create(target);
            changeTracker.addChange(Node.class, target, EntityChangeType.ADDED, domainModel);
            domainModel.completeTransaction();
        }

        nodeId = target.getId();

        NodeHandler handler = DependencyResolver.resolve(moduleProvider.getNodeHandler(handlerId).getValue1());

        customizable = handler.getSettingsModel() != null;

        if (customizable) {
            settingsModelId = modelCache.getDefinition(handler.getSettingsModel()).getId();
        }

        return this;
    }
    
    public Object updateNode() {
        RecordModelCache modelCache = dataCache.get(RecordModelCache.class);
        RecordModelDefinition definition = modelCache.getDefinition(NodeSettingsModel.class);
        try {
            definition.validateInput(JsonSerializer.serializeToTree(this), false, null);
        } catch (RecordValidationException ex) {
            LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, ex);
        }

        try (DomainModel domainModel = domainModelProvider.createWithTransaction())
        {
            Node record = domainModel.query().find(Node.class, nodeId);

            record.setTitle(title);
            record.setParentId(parentId);
            record.setPath(path);
            record.setEnabled(enabled);
            record.setAddToMenu(addToMenu);

            domainModel.update(record);
            changeTracker.addChange(Node.class, record, EntityChangeType.UPDATED, domainModel);

            domainModel.completeTransaction();
        }

        return this;
    }

    public Object deleteNode() {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            Node node = domainModel.query().find(Node.class, nodeId);
            OptionalLong parentId = node.getParentId();
            domainModel.delete(Node.class, node.getId());
            changeTracker.addChange(Node.class, node, EntityChangeType.REMOVED, domainModel);

            List<Node> nodes = domainModel.query().from(Node.class).where("parentId = $", parentId).toList(Node.class);

            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getPosition() != i) {
                    nodes.get(i).setPosition(i);
                    domainModel.update(nodes.get(i));
                    changeTracker.addChange(Node.class, nodes.get(i), EntityChangeType.UPDATED, domainModel);
                }
            }

            domainModel.completeTransaction();
        }

        return null;
    }

    private void normalizeTree(List<NodeSettingsModel> nodes, NodeSettingsModel rootNode, DomainModel domainModel, Set<Long> changedNodes) {
        nodes.forEach(node -> node.getChildren().sort((n1, n2) -> Integer.compare(n1.getPosition(), n2.getPosition())));
        
        if (rootNode != null) {
            nodes.stream().filter(n -> n.getNodeId() != rootNode.getNodeId() && !n.getParentId().isPresent()).forEach(node -> {
                rootNode.getChildren().add(node);
                node.setParentId(OptionalLong.of(rootNode.getNodeId()));
                Node record = domainModel.query().find(Node.class, node.getNodeId());
                if (record.getParentId() != node.getParentId()) {
                    record.setParentId(node.getParentId());
                    domainModel.update(record);
                }

                changedNodes.add(node.getNodeId());
            });
        }

        for (NodeSettingsModel node : nodes) {
            for (int i = 0; i < node.getChildren().size(); i++) {
                NodeSettingsModel child = node.getChildren().get(i);

                if (child.getPosition() != i) {
                    child.setPosition(i);
                    Node record = domainModel.query().find(Node.class, child.getNodeId());
                    if (record.getPosition() != i) {
                        record.setPosition(i);
                        domainModel.update(record);
                    }

                    changedNodes.add(record.getId());
                }
            }
        }
    }

    public NodeSettingsModel getRootNode() {
        NodeSettingsModel rootNode;
        RecordModelCache modelCache = dataCache.get(RecordModelCache.class);
        List<NodeSettingsModel> nodes = new ArrayList<>();
        Map<Long, NodeSettingsModel> nodeIds = new HashMap<>();

        try (DomainModel domainModel = domainModelProvider.create())
        {
            domainModel.query()
                    .from(Node.class)
                    .toList(Node.class)
                    .stream()
                    .sorted((n1, n2) -> Integer.compare(n1.getPosition(), n2.getPosition()))
                    .forEach(source -> {
                NodeSettingsModel node = DependencyResolver.resolve(NodeSettingsModel.class);

                NodeHandler handler = null;

                if (source.getHandlerId() != null) {
                    Tuple<Class<? extends NodeHandler>, String> handlerReference = moduleProvider.getNodeHandler(source.getHandlerId());
                    if (handlerReference != null) {
                        handler = DependencyResolver.resolve(handlerReference.getValue1());
                    }
                }

                node.setParentId(source.getParentId());
                node.setEnabled(source.isEnabled());
                node.setHandlerId(source.getHandlerId());
                node.setNodeId(source.getId());
                node.setAddToMenu(source.isAddToMenu());
                node.setPosition(source.getPosition());
                
                if (handler != null && handler.getSettingsModel() != null) {
                    if (source.getSettings() != null) {
                        node.setSettings(JsonSerializer.deserialize(handler.getSettingsModel(), source.getSettings()));
                    }
                    node.setSettingsModelId(modelCache.getDefinition(handler.getSettingsModel()).getId());
                }
                node.setTitle(source.getTitle());
                node.setPath(source.getPath());
                node.setCustomizable(node.getSettingsModelId() != null);

                nodes.add(node);
                nodeIds.put(node.getNodeId(), node);
            });

            Set<Long> changedNodes = new HashSet<>();

            for (NodeSettingsModel node : nodes) {
                if (!node.getParentId().isPresent()) {
                    continue;
                }

                NodeSettingsModel parent = nodeIds.get(node.getParentId().getAsLong());
                if (parent == null) {
                    node.setParentId(null);
                    Node record = domainModel.query().find(Node.class, node.getNodeId());
                    record.setParentId(null);
                    domainModel.update(record);
                    changedNodes.add(node.getNodeId());
                }
                else {
                    parent.getChildren().add(node);
                }
            }
            
            Optional<NodeSettingsModel> r = nodes.stream().filter(n -> !n.getParentId().isPresent()).findFirst();
            rootNode = r.isPresent() ? r.get() : null;

            normalizeTree(nodes, rootNode, domainModel, changedNodes);

            if (changedNodes.size() > 0) {
                changedNodes.stream().forEach(nodeId -> changeTracker.addChange(Node.class, nodeId, EntityChangeType.UPDATED, domainModel));
            }
        }

        return rootNode;
    }


    public Object changeSettings() {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            Node node = domainModel.query().find(Node.class, nodeId);

            node.setSettings(settings != null ? JsonSerializer.serialize(settings) : null);

            domainModel.update(node);
            changeTracker.addChange(Node.class, node, EntityChangeType.UPDATED, domainModel);

            domainModel.completeTransaction();
        }

        return settings;
    }

    public JsonElement updateParent() {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            Node node = domainModel.query().find(Node.class, nodeId);

            Set<Long> changedNodes = new HashSet<>();

            if (node.getParentId().isPresent() != parentId.isPresent()) {
                Node newRootNode;
                Node oldRootNode;

                if (node.getParentId().isPresent()) {
                    newRootNode = node;
                    oldRootNode = domainModel.query().from(Node.class).where("parentId IS NULL").first(Node.class);
                }
                else {
                    oldRootNode = node;
                    newRootNode = domainModel.query().find(Node.class, parentId.getAsLong());
                }

                if (!newRootNode.getParentId().isPresent()) {
                    throw new IllegalArgumentException(LanguageHelper.getText(Constants.ModuleType.MAIN, TextIds.CANNOT_HAVE_TWO_ROOT_NODES));
                }

                for (Node neighbor : domainModel.query()
                        .from(Node.class)
                        .where("parentId = $ AND position > $", newRootNode.getParentId().getAsLong(), newRootNode.getPosition())
                        .toList(Node.class)) {
                    neighbor.setPosition(neighbor.getPosition() - 1);
                    domainModel.update(neighbor);
                    changedNodes.add(neighbor.getId());
                }

                newRootNode.setPosition(0);
                newRootNode.setParentId(null);
                changedNodes.add(newRootNode.getId());

                for (Node neighbor : domainModel.query()
                        .from(Node.class)
                        .where("parentId = $ AND " + Constants.Data.ID_PROPERTY_NAME + " != $ AND position > 0", newRootNode.getId(), nodeId)
                        .toList(Node.class)) {
                    neighbor.setPosition(neighbor.getPosition() + 1);
                    domainModel.update(neighbor);
                    changedNodes.add(neighbor.getId());
                }

                oldRootNode.setPosition(0);
                oldRootNode.setParentId(OptionalLong.of(newRootNode.getId()));
                changedNodes.add(oldRootNode.getId());
            }
            else if (node.getParentId().isPresent()) {
                if (node.getParentId() == parentId) {
                    if (position > node.getPosition()) {
                        for (Node neighbor : domainModel.query()
                                .from(Node.class)
                                .where("parentId = $ AND " + Constants.Data.ID_PROPERTY_NAME + " != $ AND position > $ AND position <= $", parentId, nodeId, node.getPosition(), position)
                                .toList(Node.class)) {
                            neighbor.setPosition(neighbor.getPosition() - 1);
                            domainModel.update(neighbor);
                            changedNodes.add(neighbor.getId());
                        }
                    }
                    else if (position < node.getPosition()) {
                        for (Node neighbor : domainModel.query()
                                .from(Node.class)
                                .where("parentId = $ AND " + Constants.Data.ID_PROPERTY_NAME + " != $ AND position >= $ AND position < $", parentId, nodeId, position, node.getPosition())
                                .toList(Node.class)) {
                            neighbor.setPosition(neighbor.getPosition() + 1);
                            domainModel.update(neighbor);
                            changedNodes.add(neighbor.getId());
                        }
                    }
                }
                else {
                    for (Node neighbor : domainModel.query()
                            .from(Node.class)
                            .where("parentId = $ AND position > $ AND " + Constants.Data.ID_PROPERTY_NAME + " != $", node.getParentId(), node.getPosition(), nodeId)
                            .toList(Node.class)) {
                        neighbor.setPosition(neighbor.getPosition() - 1);
                        domainModel.update(neighbor);
                        changedNodes.add(neighbor.getId());
                    }

                    for (Node neighbor : domainModel.query()
                            .from(Node.class)
                            .where("parentId = $ AND position >= $ AND " + Constants.Data.ID_PROPERTY_NAME + " != $", parentId, position, nodeId)
                            .toList(Node.class)) {
                        neighbor.setPosition(neighbor.getPosition() + 1);
                        domainModel.update(neighbor);
                        changedNodes.add(neighbor.getId());
                    }
                }
            }

            node.setParentId(parentId);
            node.setPosition(position);
            changedNodes.add(nodeId);

            for (long id : changedNodes) {
                changeTracker.addChange(Node.class, id, EntityChangeType.UPDATED, domainModel);
            }

            domainModel.completeTransaction();
        }
        
        JsonElement rootNode = JsonSerializer.serializeToTree(getRootNode());

        JsonObject ret = new JsonObject();
        ret.add("Root", rootNode);
        return ret;
    }
}
