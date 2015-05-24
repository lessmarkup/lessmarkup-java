package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.Node;
import com.lessmarkup.dataobjects.NodeAccess;
import com.lessmarkup.framework.helpers.*;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.AbstractCacheHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.structure.CachedNodeAccess;
import com.lessmarkup.interfaces.structure.CachedNodeInformation;
import com.lessmarkup.interfaces.structure.ChildHandlerSettings;
import com.lessmarkup.interfaces.structure.GetNodeHandlerPreprocess;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import com.lessmarkup.interfaces.structure.NodeCache;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.Tuple;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import com.lessmarkup.userinterface.nodehandlers.DefaultRootNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.configuration.ConfigurationRootNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration.LoginNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration.DatabaseConfigurationNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.user.ForgotPasswordNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.user.UserCardRecordsNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.user.UserProfileNodeHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.logging.Level;

public class NodeCacheImpl extends AbstractCacheHandler implements NodeCache {

    private final DomainModelProvider domainModelProvider;
    private final ModuleProvider moduleProvider;
    private final DataCache dataCache;
    private CachedNodeInformation rootNode;
    private final List<CachedNodeInformation> cachedNodes = new ArrayList<>();
    private final HashMap<Long, CachedNodeInformation> idToNode = new HashMap<>();

    @Inject
    public NodeCacheImpl(DomainModelProvider domainModelProvider, ModuleProvider moduleProvider, DataCache dataCache) {
        super(new Class<?>[] {Node.class });
        this.domainModelProvider = domainModelProvider;
        this.moduleProvider = moduleProvider;
        this.dataCache = dataCache;
    }
    
    @Override
    public void initialize(OptionalLong objectId) {
        if (objectId.isPresent()) {
            throw new IllegalArgumentException();
        }
        
        List<CachedNodeInformationImpl> cachedNodesImpl = new ArrayList<>();
        
        try (DomainModel domainModel = this.domainModelProvider.create()) {
            
            domainModel.query()
                    .from(Node.class)
                    .orderBy("position")
                    .toListJava(Node.class)
                    .stream()
                    .map(n -> new CachedNodeInformationImpl(n) {})
                    .forEach(cachedNodesImpl::add);
            
            HashMap<Long, CachedNodeInformation> nodeMap = new HashMap<>();
            
            cachedNodesImpl.forEach(n -> nodeMap.put(n.getNodeId(), n));
            
            domainModel.query()
                    .from(NodeAccess.class)
                    .toListJava(NodeAccess.class)
                    .forEach(na -> {
                        CachedNodeInformation node = nodeMap.get(na.getNodeId());
                        if (node != null) {
                            node.getAccessList().add(new CachedNodeAccess(na));
                        }
                    });
        } catch (Exception ex) {
            LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, ex);
        }
        
        Optional<CachedNodeInformationImpl> root = cachedNodesImpl.stream().filter(n -> !n.getParentNodeId().isPresent()).findFirst();
        
        CachedNodeInformationImpl rootImpl;
        
        if (root.isPresent()) {
            rootImpl = root.get();
        } else {
            rootImpl = new CachedNodeInformationImpl();
            CachedNodeAccess rootAccess = new CachedNodeAccess();
            rootAccess.setAccessType(NodeAccessType.READ);
            rootImpl.getAccessList().add(rootAccess);
            rootImpl.setNodeId(1);
            rootImpl.setTitle("Home");
            rootImpl.setHandlerId(DefaultRootNodeHandler.class.getSimpleName());
            rootImpl.setHandlerType(DefaultRootNodeHandler.class);
            rootImpl.setVisible(true);
            cachedNodesImpl.add(rootImpl);
        }
        
        this.rootNode = rootImpl;
        
        initializeTree(rootImpl, cachedNodesImpl);
        
        rootImpl.setRoot(this.rootNode);
        
        addVirtualNode(ConfigurationRootNodeHandler.class, 
                Constants.NodePathConfiguration(),
                LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.CONFIGURATION),
                Constants.ModuleTypeMain(),
                NodeAccessType.NO_ACCESS);
        
        SiteConfiguration siteConfiguration = this.dataCache.get(SiteConfiguration.class);
        
        String adminLoginPage = siteConfiguration.getAdminLoginPage();
        if (adminLoginPage == null || adminLoginPage.length() == 0) {
            adminLoginPage = RequestContextHolder.getContext().getEngineConfiguration().getAdminLoginPage();
        }
        
        if (StringHelper.isNullOrEmpty(adminLoginPage)) {
            adminLoginPage = Constants.NodePathAdminLoginDefaultPage();
        }
        
        addVirtualNode(LoginNodeHandler.class,
                adminLoginPage,
                LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.ADMINISTRATOR_LOGIN),
                Constants.ModuleTypeMain(),
                NodeAccessType.READ);
        
        CachedNodeInformationImpl node = addVirtualNode(UserProfileNodeHandler.class,
                Constants.NodePathProfile(),
                LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.USER_PROFILE),
                Constants.ModuleTypeMain(),
                NodeAccessType.READ);
        
        node.setLoggedIn(true);
        
        if (siteConfiguration.getHasUsers()) {
            addVirtualNode(UserCardRecordsNodeHandler.class,
                    Constants.NodePathUserCards(),
                    LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.USER_CARDS),
                    Constants.ModuleTypeMain(),
                    NodeAccessType.READ);
            addVirtualNode(ForgotPasswordNodeHandler.class,
                    Constants.NodePathForgotPassword(),
                    LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.FORGOT_PASSWORD),
                    Constants.ModuleTypeMain(),
                    NodeAccessType.READ);
        }
    }
    
    private void initializeTree(CachedNodeInformationImpl node, List<CachedNodeInformationImpl> allNodes) {
        if (node.getHandlerId() != null && node.getHandlerId().length() > 0) {
            Tuple<Class<? extends NodeHandler>,String> handler = this.moduleProvider.getNodeHandler(node.getHandlerId());
            if (handler != null) {
                node.setHandlerType(handler.getValue1());
                node.setHandlerModuleType(handler.getValue2());
                
            }
        }
        
        node.setRoot(this.rootNode);
        
        if (node.getParent() == null) {
            // root
            node.setPath("");
            node.setFullPath("/");
        } else {
            node.setPath(node.getPath().trim().toLowerCase());
            
            if (node.getPath().length() == 0) {
                return;
            }
            
            node.setFullPath(node.getParent() == this.rootNode ? ("/" + node.getPath()) : (node.getParent().getFullPath() + "/" + node.getPath()));
        }
        
        this.cachedNodes.add(node);
        this.idToNode.put(node.getNodeId(), node);
        
        for (CachedNodeInformationImpl child : allNodes) {
            if (!child.getParentNodeId().isPresent() || child.getParentNodeId().getAsLong() != node.getNodeId() || !node.isEnabled()) {
                continue;
            }
            node.getChildren().add(child);
            child.setParent(node);
            initializeTree(child, allNodes);
        }
    }
    
    private <T extends NodeHandler> CachedNodeInformationImpl addVirtualNode(Class<T> type, String path, String title, String moduleType, NodeAccessType accessType) {
        Optional<Long> nodeId = this.idToNode.keySet().stream().max(Long::compare);
        
        CachedNodeInformationImpl node = new CachedNodeInformationImpl();
        node.setNodeId(nodeId.orElse(0l) + 1);
        node.setFullPath("/" + path.toLowerCase());
        node.setPath(path.toLowerCase());
        node.setHandlerModuleType(moduleType);
        node.setParentNodeId(OptionalLong.of(this.rootNode.getNodeId()));
        node.setParent(this.rootNode);
        node.setTitle(title);
        node.setHandlerType(type);
        node.setHandlerId(path);
        node.setRoot(this.rootNode);
        node.setVisible(false);
        
        CachedNodeAccess cachedNodeAccess = new CachedNodeAccess();
        cachedNodeAccess.setAccessType(accessType);
        node.getAccessList().add(cachedNodeAccess);
        
        this.rootNode.getChildren().add(node);
        this.idToNode.put(node.getNodeId(), node);
        
        return node;
    }

    @Override
    public CachedNodeInformation getNode(long nodeId) {
        return idToNode.get(nodeId);
    }

    @Override
    public Tuple<CachedNodeInformation, String> getNode(String path) {
        List<String> nodeParts = new ArrayList<>();
        if (path != null) {
            Arrays.stream(path.split("/")).map(String::trim).filter(p -> p.length() > 0).forEach(nodeParts::add);
        }
        
        if (nodeParts.isEmpty()) {
            return new Tuple<>(this.rootNode, "");
        }
        
        CachedNodeInformation node = this.rootNode;
        
        int pos;
        
        for (pos = 0; pos < nodeParts.size(); pos++) {
            String pathPart = nodeParts.get(pos);
            
            Optional<CachedNodeInformation> child = node.getChildren().stream().filter(n -> n.getPath().equals(pathPart)).findFirst();
            
            if (!child.isPresent()) {
                break;
            }
            
            node = child.get();
        }
        
        String rest = "";
        
        if (pos < nodeParts.size()) {
            rest = StringHelper.join(rest, nodeParts.stream().skip(pos).iterator());
        }
        
        return new Tuple<>(node, rest);
    }

    @Override
    public CachedNodeInformation getRootNode() {
        return this.rootNode;
    }

    @Override
    public List<CachedNodeInformation> getNodes() {
        return this.cachedNodes;
    }
    
    private boolean traverseParents(CachedNodeInformation node, GetNodeHandlerPreprocess filter) {
        if (node.getParent() != null) {
            if (traverseParents(node.getParent(), filter)) {
                return true;
            }
        }
        
        return filter.preprocess(null, node.getTitle(), node.getFullPath(), null, OptionalLong.of(node.getNodeId()));
    }

    @Override
    public NodeHandler getNodeHandler(String path, GetNodeHandlerPreprocess filter) {
        if (path != null) {
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (path != null) {
            int queryPost = path.indexOf('?');
            if (queryPost >= 0) {
                path = path.substring(0, queryPost);
            }
        }
        
        Tuple<CachedNodeInformation, String> foundNode = getNode(path);
       
        if (foundNode == null) {
            LoggingHelper.getLogger(getClass()).log(Level.INFO, "Cannot get node for path ''{0}''", path);
            return null;
        }
        
        CachedNodeInformation node = foundNode.getValue1();

        CurrentUser currentUser = RequestContextHolder.getContext().getCurrentUser();

        if (node.isLoggedIn() && !currentUser.getUserId().isDefined()) {
            LoggingHelper.getLogger(getClass()).info("This node requires user to be logged in");
            return null;
        }

        LoggingHelper.getLogger(getClass()).info("Checking node access rights");
        
        NodeAccessType accessType = node.checkRights(currentUser);
        
        if (accessType == NodeAccessType.NO_ACCESS) {
            LoggingHelper.getLogger(getClass()).info("Current user has no access to specified node");
            return null;
        }
        
        if (filter != null && node.getParent() != null) {
            traverseParents(node.getParent(), filter);
        }
        
        if (node.getHandlerType() == null) {
            LoggingHelper.getLogger(getClass()).log(Level.WARNING, "Node handler is not set for node path ''{0}''", path);
            return null;
        }

        Class<? extends NodeHandler> handlerType = node.getHandlerType();

        if (handlerType.equals(DefaultRootNodeHandler.class) && StringHelper.isNullOrEmpty(RequestContextHolder.getContext().getEngineConfiguration().getConnectionString())) {
            handlerType = DatabaseConfigurationNodeHandler.class;
        }
        
        NodeHandler nodeHandler = DependencyResolver.resolve(handlerType);
        
        if (nodeHandler == null) {
            return null;
        }
        
        String currentTitle = node.getTitle();
        String currentPath = node.getFullPath();
        
        String settingsText = node.getSettings();
        
        JsonElement settingsElement = null;
        
        if (settingsText != null) {
            settingsElement = JsonSerializer.deserializeToTree(settingsText);
        }
        
        nodeHandler.initialize(OptionalLong.of(node.getNodeId()), 
                settingsElement != null && settingsElement.isJsonObject() ? settingsElement.getAsJsonObject() : null, 
                node.getPath(), node.getFullPath(), accessType);
        
        boolean first = true;
        
        String rest = foundNode.getValue2();
        
        while (rest != null && rest.length() > 0) {
            if (filter != null && filter.preprocess(nodeHandler, currentTitle, currentPath, rest, first ? OptionalLong.of(node.getNodeId()) : null)) {
                return null;
            }
            
            first = false;
            
            ChildHandlerSettings childSettings = nodeHandler.getChildHandler(rest);
            
            if (childSettings == null) {
                return null;
            }
            
            nodeHandler = childSettings.getHandler();
            
            currentTitle = childSettings.getTitle();
            currentPath += "/" + childSettings.getPath();
            
            if (childSettings.getRest() == null || childSettings.getRest().length() == 0) {
                break;
            }
            
            rest = childSettings.getRest();
        }
        
        if (filter != null && filter.preprocess(nodeHandler, currentTitle, currentPath, rest, first ? OptionalLong.of(node.getNodeId()) : OptionalLong.empty())) {
            return null;
        }
        
        return nodeHandler;
    }
}
