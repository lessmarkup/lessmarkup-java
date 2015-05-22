/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.module;

import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.module.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Implements(ModuleIntegration.class)
public class ModuleIntegrationImpl implements ModuleIntegration {

    private final Map<Class<?>, EntitySearch> entitySearch = new HashMap<>();
    private final List<UserPropertyProvider> userPropertyProviders = new LinkedList<>();
    private String registerindModuleType;
    private final Map<String, ModuleActionHandler> moduleActionHandlers = new HashMap<>();
    
    @Override
    public void registerBackgroundJobHandler(BackgroundJobHandler handler) {
    }

    @Override
    public boolean doBackgroundJobs() {
        return true;
    }

    @Override
    public void registerEntitySearch(Class<? extends DataObject> type, EntitySearch entitySearch) {
        this.entitySearch.put(type, entitySearch);
    }

    @Override
    public void registerUserPropertyProvider(UserPropertyProvider provider) {
        userPropertyProviders.add(provider);
    }

    @Override
    public EntitySearch getEntitySearch(Class<? extends DataObject> collectionType) {
        return entitySearch.get(collectionType);
    }

    @Override
    public Collection<UserProperty> getUserProperties(long userId) {
        List<UserProperty> ret = new ArrayList<>();
        
        userPropertyProviders.forEach(provider -> {
            provider.getProperties(userId).forEach(ret::add);
        });
        
        return ret;
    }
    
    public String getRegisteringModuleType() {
        return registerindModuleType;
    }
    
    public void setRegisteringModuleType(String value) {
        registerindModuleType = value;
    }

    @Override
    public void registerActionHandler(String name, ModuleActionHandler handler) {
        moduleActionHandlers.put(name, handler);
    }

    @Override
    public ModuleActionHandler getActionHandler(String name) {
        return moduleActionHandlers.get(name);
    }
}
