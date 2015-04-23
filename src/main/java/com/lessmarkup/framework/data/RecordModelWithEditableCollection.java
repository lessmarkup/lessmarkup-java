/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.framework.data;

import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.recordmodel.ModelCollection;
import com.lessmarkup.interfaces.recordmodel.RecordModel;

public abstract class RecordModelWithEditableCollection<TM extends RecordModel, TD extends AbstractDataObject> extends RecordModel<TM> {
    
    private final Class<TM> modelType;
    
    protected RecordModelWithEditableCollection(String titleTextId, Class<TD> dataType, Class<TM> modelType, boolean submitWithCaptcha) {
        super(titleTextId, null, dataType, submitWithCaptcha);
        this.modelType = modelType;
    }

    protected RecordModelWithEditableCollection(Class<TD> dataType, Class<TM> modelType) {
        super(null, dataType);
        this.modelType = modelType;
    }

    protected RecordModelWithEditableCollection(String titleTextId, Class<TD> dataType, Class<TM> modelType) {
        super(titleTextId, null, dataType);
        this.modelType = modelType;
    }
    
    @Override
    public ModelCollection<TM> createCollection() {
        return new RecordModelEditableCollection(
                DependencyResolver.resolve(DomainModelProvider.class),
                DependencyResolver.resolve(DataCache.class), 
                DependencyResolver.resolve(ChangeTracker.class),
                this.modelType,
                getDataType()
        );
    }
}
