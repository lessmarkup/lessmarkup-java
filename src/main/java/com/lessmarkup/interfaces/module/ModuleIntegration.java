package com.lessmarkup.interfaces.module;

import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.Tuple;
import java.util.Collection;

public interface ModuleIntegration {
    void registerBackgroundJobHandler(BackgroundJobHandler handler);
    boolean doBackgroundJobs();
    void registerEntitySearch(Class<? extends DataObject> type, EntitySearch entitySearch);
    void registerUserPropertyProvider(UserPropertyProvider provider);
    EntitySearch getEntitySearch(Class<? extends DataObject> collectionType);
    Collection<UserProperty> getUserProperties(long userId);
    void registerActionHandler(String name, ModuleActionHandler handler);
    ModuleActionHandler getActionHandler(String name);
}
