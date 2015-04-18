package com.lessmarkup.userinterface.nodehandlers.user;

import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.userinterface.nodehandlers.common.TabPageNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserProfileNodeHandler extends TabPageNodeHandler {

    @Autowired
    public UserProfileNodeHandler(DataCache dataCache, CurrentUser currentUser) {
        super(dataCache, currentUser);
    }
}
