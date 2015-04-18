package com.lessmarkup.interfaces.recordmodel;

import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.data.AbstractDataObject;

public abstract class RecordModel<T extends RecordModel> {
    private final Class<? extends ModelCollection<T>> collectionType;
    private final Class<? extends AbstractDataObject> dataType;
    private final String titleTextId;
    private final boolean submitWithCaptcha;
    private long id;
    
    protected RecordModel(String titleTextId, Class<? extends ModelCollection<T>> collectionType, Class<? extends AbstractDataObject> dataType, boolean submitWithCaptcha) {
        this.collectionType = collectionType;
        this.dataType = dataType;
        this.titleTextId = titleTextId;
        this.submitWithCaptcha = submitWithCaptcha;
    }
    
    protected RecordModel(String titleTextId, Class<? extends ModelCollection<T>> collectionType, Class<? extends AbstractDataObject> dataType) {
        this(titleTextId, collectionType, dataType, false);
    }
    
    protected RecordModel(String titleTextId, Class<? extends ModelCollection<T>> collectionType) {
        this(titleTextId, collectionType, null);
    }
    
    protected RecordModel(String titleTextId) {
        this(titleTextId, null);
    }

    protected RecordModel(String titleTextId, boolean submitWithCaptcha) {
        this(titleTextId, null, null, submitWithCaptcha);
    }

    protected RecordModel(Class<? extends ModelCollection<T>> collectionType, Class<? extends AbstractDataObject> dataType) {
        this(null, collectionType, dataType);
    }
    
    protected RecordModel() {
        this(null);
    }
    
    public Class<? extends ModelCollection<T>> getCollectionType() {
        return collectionType;
    }
    
    public Class<? extends AbstractDataObject> getDataType() {
        return dataType;
    }
    
    public String getTitleTextId() {
        return titleTextId;
    }
    
    public boolean getSubmitWithCaptcha() {
        return submitWithCaptcha;
    }
    
    public ModelCollection<T> createCollection() {
        return DependencyResolver.resolve(getCollectionType());
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
}
