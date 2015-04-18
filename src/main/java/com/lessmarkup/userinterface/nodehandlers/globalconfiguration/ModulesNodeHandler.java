package com.lessmarkup.userinterface.nodehandlers.globalconfiguration;

import com.google.gson.JsonObject;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.Module;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.structure.ConfigurationHandler;
import com.lessmarkup.interfaces.structure.RecordAction;
import com.lessmarkup.userinterface.model.global.ModuleModel;
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationHandler(titleTextId = TextIds.MODULES)
@Component
@Scope("prototype")
public class ModulesNodeHandler extends RecordListNodeHandler<ModuleModel> {

    private final ChangeTracker changeTracker;
    private final DomainModelProvider domainModelProvider;

    @Autowired
    public ModulesNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache, ChangeTracker changeTracker) {
        super(domainModelProvider, dataCache, ModuleModel.class);
        this.domainModelProvider = domainModelProvider;
        this.changeTracker = changeTracker;
    }

    @RecordAction(nameTextId = TextIds.ENABLE, visible = "!enabled")
    public JsonObject enableModule(long recordId, String filter) {
        return enableModule(recordId, true);
    }

    public JsonObject disableModule(long recordId, String filter) {
        return enableModule(recordId, false);
    }

    protected JsonObject enableModule(long moduleId, boolean enable) {
        try (DomainModel domainModel = domainModelProvider.create()) {
            Module siteModule = domainModel.query().find(Module.class, moduleId);

            siteModule.setEnabled(enable);
            domainModel.update(siteModule);
            changeTracker.addChange(Module.class, siteModule, EntityChangeType.UPDATED, domainModel);

            ModuleModel.ModuleModelCollection collection = DependencyResolver.resolve(ModuleModel.ModuleModelCollection.class);
            collection.initialize(getObjectId(), getAccessType());

            List<Long> recordIds = new ArrayList<>();
            recordIds.add(moduleId);
            ModuleModel record = collection.read(domainModel.query(), recordIds).iterator().next();

            JsonObject ret = new JsonObject();
            ret.addProperty("index", getIndex(record, null, domainModel));
            ret.add("record", JsonSerializer.serializeToTree(record));
            return ret;
        }
    }
}
