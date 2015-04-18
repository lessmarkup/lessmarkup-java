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
    protected RecordModelWithEditableCollection(String titleTextId, Class<TD> dataType, boolean submitWithCaptcha) {
        super(titleTextId, null, dataType, submitWithCaptcha);
    }

    protected RecordModelWithEditableCollection(Class<TD> dataType) {
        super(null, dataType);
    }

    protected RecordModelWithEditableCollection(String titleTextId, Class<TD> dataType) {
        super(titleTextId, null, dataType);
    }
    
    @Override
    public ModelCollection<TM> createCollection() {
        return new RecordModelEditableCollection(
                DependencyResolver.resolve(DomainModelProvider.class),
                DependencyResolver.resolve(DataCache.class), 
                DependencyResolver.resolve(ChangeTracker.class),
                getCollectionType(),
                getDataType()
        );
    }
}
