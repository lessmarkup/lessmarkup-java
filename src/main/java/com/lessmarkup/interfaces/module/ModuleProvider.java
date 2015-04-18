package com.lessmarkup.interfaces.module;

import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.Tuple;

import java.util.Collection;

public interface ModuleProvider {
    Collection<ModuleConfiguration> getModules();
    void discoverAndRegisterModules();
    void updateModuleDatabase(DomainModelProvider domainModelProvider);
    Collection<String> getNodeHandlers();
    Tuple<Class<? extends NodeHandler>, String> getNodeHandler(String id);
}
