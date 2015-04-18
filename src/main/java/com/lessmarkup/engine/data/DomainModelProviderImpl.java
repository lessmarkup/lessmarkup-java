package com.lessmarkup.engine.data;

import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class DomainModelProviderImpl implements DomainModelProvider {

    private final ModuleProvider moduleProvider;

    @Autowired
    public DomainModelProviderImpl(ModuleProvider moduleProvider) {
        this.moduleProvider = moduleProvider;
    }
    
    @Override
    public void initialize() {
        for (ModuleConfiguration module : moduleProvider.getModules()) {
            for (Class<? extends DataObject> dataObject : module.getInitializer().getDataObjectTypes()) {
                DomainModelImpl.registerDataType(dataObject);
            }
        }
    }
    
    @Override
    public DomainModel create() {
        DomainModelImpl domainModel = new DomainModelImpl();
        domainModel.createConnection(null);
        return domainModel;
    }

    @Override
    public DomainModel create(String connectionString) {
        DomainModelImpl domainModel = new DomainModelImpl();
        domainModel.createConnection(connectionString);
        return domainModel;
    }

    @Override
    public DomainModel createWithTransaction() {
        DomainModelImpl domainModel = new DomainModelImpl();
        domainModel.createConnectionWithTransaction();
        return domainModel;
    }

    @Override
    public int getCollectionId(Class<?> collectionType) {
        return DomainModelImpl.getCollectionId(collectionType);
    }
}
