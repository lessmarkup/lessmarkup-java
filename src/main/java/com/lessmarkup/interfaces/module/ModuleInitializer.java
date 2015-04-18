package com.lessmarkup.interfaces.module;

import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.data.Migration;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.structure.NodeHandler;
import java.util.Collection;

public interface ModuleInitializer {
    default void initialize() {}
    String getName();
    String getModuleType();
    Collection<Class<? extends RecordModel>> getModelTypes();
    Collection<Class<? extends DataObject>> getDataObjectTypes();
    Collection<Class<? extends NodeHandler>> getNodeHandlerTypes();
    Collection<Class<? extends Migration>> getMigrations();
}
