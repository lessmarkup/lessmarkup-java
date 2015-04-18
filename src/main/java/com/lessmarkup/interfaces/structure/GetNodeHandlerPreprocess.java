package com.lessmarkup.interfaces.structure;

import java.util.OptionalLong;

public interface GetNodeHandlerPreprocess {
    boolean preprocess(NodeHandler nodeHandler, String title, String path, String rest, OptionalLong nodeId);
}
