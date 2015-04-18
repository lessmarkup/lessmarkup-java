package com.lessmarkup.userinterface.nodehandlers.user;

import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.UserCardHandler;
import com.lessmarkup.interfaces.structure.UserCardNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.common.TabPageNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.OptionalLong;

@Component
@Scope("prototype")
public class UserCardTabsNodeHandler extends TabPageNodeHandler {

    private final ModuleProvider moduleProvider;
    private long userId;

    @Autowired
    public UserCardTabsNodeHandler(DataCache dataCache, CurrentUser currentUser, ModuleProvider moduleProvider) {
        super(dataCache, currentUser);
        this.moduleProvider = moduleProvider;
    }

    public void initialize(long userId)
    {
        this.userId = userId;

        for (ModuleConfiguration module : moduleProvider.getModules()) {
            for (Class<? extends NodeHandler> type : module.getInitializer().getNodeHandlerTypes()) {

                UserCardHandler handlerAttribute = type.getAnnotation(UserCardHandler.class);

                if (handlerAttribute == null)
                {
                    continue;
                }

                addPage(type, LanguageHelper.getText(module.getModuleType(), handlerAttribute.titleTextId()), handlerAttribute.path(), OptionalLong.of(userId));
            }
        }
    }

    @Override
    protected NodeHandler createChildHandler(Class<? extends NodeHandler> handlerType) {
        NodeHandler handler = super.createChildHandler(handlerType);

        UserCardNodeHandler userCardNodeHandler = (UserCardNodeHandler) handler;
        if (userCardNodeHandler != null) {
            userCardNodeHandler.initialize(userId);
        }

        return handler;
    }
}
