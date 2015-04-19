package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.google.gson.JsonObject;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.userinterface.model.global.UserBlockModel;
import com.lessmarkup.userinterface.model.global.UserModel;
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@ConfigurationHandler(titleTextId = TextIds.USERS)
public class SiteUsersNodeHandler extends RecordListNodeHandler<UserModel> {

    private final DomainModelProvider domainModelProvider;

    @Autowired
    public SiteUsersNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache) {
        super(domainModelProvider, dataCache, UserModel.class);
        this.domainModelProvider = domainModelProvider;
    }

    public JsonObject block(long recordId, UserBlockModel newObject) {
        newObject.blockUser(recordId);

        try (DomainModel domainModel = domainModelProvider.create()) {
            List<Long> recordIds = new ArrayList<>();
            recordIds.add(recordId);
            UserModel user = getCollection().read(domainModel.query(), recordIds).iterator().next();
            return returnRecordResult(user);
        }
    }

    public JsonObject unblock(long recordId) {
        UserBlockModel model = DependencyResolver.resolve(UserBlockModel.class);
        model.unblockUser(recordId);

        try (DomainModel domainModel = domainModelProvider.create()) {
            List<Long> recordIds = new ArrayList<>();
            recordIds.add(recordId);
            UserModel user = getCollection().read(domainModel.query(), recordIds).iterator().next();
            return returnRecordResult(user);
        }
    }
}
