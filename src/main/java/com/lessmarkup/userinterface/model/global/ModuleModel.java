package com.lessmarkup.userinterface.model.global;

import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.Module;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.data.QueryBuilder;
import com.lessmarkup.interfaces.recordmodel.ModelCollection;
import com.lessmarkup.interfaces.recordmodel.RecordColumn;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@Scope("prototype")
public class ModuleModel extends RecordModel<ModuleModel> {

    @Component
    @Scope("prototype")
    public static class ModuleModelCollection implements ModelCollection<ModuleModel> {

        private final DomainModelProvider domainModelProvider;

        @Autowired
        public ModuleModelCollection(DomainModelProvider domainModelProvider) {
            this.domainModelProvider = domainModelProvider;
        }

        @Override
        public List<Long> readIds(QueryBuilder query, boolean ignoreOrder) {
            return query.from(Module.class).where("removed = $ AND system = $", false, false).toIdList();
        }

        @Override
        public int getCollectionId() {
            return domainModelProvider.getCollectionId(Module.class).getAsInt();
        }

        @Override
        public Collection<ModuleModel> read(QueryBuilder queryBuilder, List<Long> ids) {
            Collection<ModuleModel> ret = new ArrayList<>();

            for (Module module : queryBuilder.from(Module.class).where("removed = $ AND system = $", false, false).whereIds(ids).toList(Module.class)) {
                ModuleModel model = new ModuleModel();
                model.setEnabled(module.isEnabled());
                model.setName(module.getName());
                model.setModuleId(module.getId());
                ret.add(model);
            }

            return ret;
        }
    }

    private long moduleId;
    private String name;
    private boolean enabled;

    public ModuleModel() {
        super(ModuleModelCollection.class, Module.class);
    }

    public long getModuleId() {
        return moduleId;
    }

    public void setModuleId(long moduleId) {
        this.moduleId = moduleId;
    }

    @RecordColumn(textId = TextIds.NAME)
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    @RecordColumn(textId = TextIds.ENABLED)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public boolean isEnabled() {
        return enabled;
    }
}
