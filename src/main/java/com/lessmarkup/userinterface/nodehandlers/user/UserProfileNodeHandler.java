package com.lessmarkup.userinterface.nodehandlers.user;

import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.userinterface.nodehandlers.common.TabPageNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserProfileNodeHandler extends TabPageNodeHandler {

    @Autowired
    public UserProfileNodeHandler(DataCache dataCache) {
        super(dataCache);
    }
}
