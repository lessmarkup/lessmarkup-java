package com.lessmarkup.userinterface.nodehandlers.user;

import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.ChildHandlerSettings;
import com.lessmarkup.interfaces.system.UserCache;
import com.lessmarkup.userinterface.model.user.UserCardModel;
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.OptionalLong;

@Component
@Scope("prototype")
public class UserCardRecordsNodeHandler extends RecordListNodeHandler<UserCardModel> {

    private final DataCache dataCache;

    @Autowired
    public UserCardRecordsNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache) {
        super(domainModelProvider, dataCache, UserCardModel.class);
        this.dataCache = dataCache;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public ChildHandlerSettings getChildHandler(String path) {
        String[] parts = path.split("/");
        if (parts.length == 0) {
            return null;
        }

        long userId;
        try {
            userId = Long.parseLong(parts[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        UserCardTabsNodeHandler handler = DependencyResolver.resolve(UserCardTabsNodeHandler.class);

        handler.initialize(userId);

        handler.initialize(null, null, parts[0], "", getAccessType());

        UserCache userCache = dataCache.get(UserCache.class, OptionalLong.of(userId));

        ChildHandlerSettings ret = new ChildHandlerSettings();
        ret.setHandler(handler);
        ret.setId(OptionalLong.of(userId));
        ret.setPath(String.join("/", Arrays.stream(parts).skip(1).toArray(String[]::new)));
        ret.setTitle(userCache.getName());
        return ret;
    }
}
