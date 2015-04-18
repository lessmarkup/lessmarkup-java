package com.lessmarkup.interfaces.structure;

import com.lessmarkup.interfaces.cache.CacheHandler;
import java.util.List;

public interface NodeCache extends CacheHandler {
    CachedNodeInformation getNode(long nodeId);
    Tuple<CachedNodeInformation, String> getNode(String path);
    CachedNodeInformation getRootNode();
    List<CachedNodeInformation> getNodes();
    NodeHandler getNodeHandler(String path, GetNodeHandlerPreprocess filter);
    default NodeHandler getNodeHandler(String path) {
        return getNodeHandler(path, null);
    }
}
