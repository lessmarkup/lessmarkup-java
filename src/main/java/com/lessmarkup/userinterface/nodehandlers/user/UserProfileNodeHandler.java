package com.lessmarkup.userinterface.nodehandlers.user;

import com.google.inject.Inject;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.userinterface.nodehandlers.common.TabPageNodeHandler;

public class UserProfileNodeHandler extends TabPageNodeHandler {

    @Inject
    public UserProfileNodeHandler(DataCache dataCache) {
        super(dataCache);
    }
}
