/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.framework.data;

import com.lessmarkup.Constants;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.data.QueryBuilder;
import com.lessmarkup.interfaces.recordmodel.EditableModelCollection;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordModelEditableCollection<TM extends RecordModel, TD extends DataObject> implements EditableModelCollection<TM> {

    private final DomainModelProvider domainModelProvider;
    private final ChangeTracker changeTracker;
    private final Class<TM> modelType;
    private final Class<TD> dataType;
    private final List<RecordToDataPropertyMapper> properties;
    
    public RecordModelEditableCollection(DomainModelProvider domainModelProvider, DataCache dataCache, ChangeTracker changeTracker, Class<TM> recordModelType, Class<TD> dataType) {
        properties = dataCache.get(EditableCollectionCache.class).getProperties(recordModelType, dataType);
        this.domainModelProvider = domainModelProvider;
        this.changeTracker = changeTracker;
        this.modelType = recordModelType;
        this.dataType = dataType;
    }
    
    @Override
    public List<Long> readIds(QueryBuilder query, boolean ignoreOrder) {
        return query.from(dataType).toIdList();
    }

    @Override
    public int getCollectionId() {
        return domainModelProvider.getCollectionId(dataType).getAsInt();
    }
    
    protected void updateModel(TM model, TD record) {
        for (RecordToDataPropertyMapper property : properties) {
            Object data = property.getDataValue(record);
            property.setModelValue(model, data);
        }
    }
    
    protected void updateData(TD record, TM model) {
        for (RecordToDataPropertyMapper property : properties) {
            Object data = property.getModelValue(model);
            if (data == null && property.getFieldType().equals(byte[].class)) {
                // it means the data is not changed
                continue;
            }
            property.setDataValue(record, data);
        }
    }
    
    @Override
    public Collection<TM> read(QueryBuilder queryBuilder, List<Long> ids) {
        List<String> idsString = new LinkedList<>();
        List<TM> ret = new ArrayList<>();
        ids.forEach(s -> idsString.add(s.toString()));
        try (DomainModel domainModel = domainModelProvider.create()) {
            for (TD record : domainModel.query().from(dataType).where(String.format(Constants.DataIdPropertyName() + " in (%s)", String.join(",", idsString))).toList(dataType)) {
                TM model = DependencyResolver.resolve(modelType);
                updateModel(model, record);
                ret.add(model);
            }
        }
        return ret;
    }

    @Override
    public void initialize(OptionalLong objectId, NodeAccessType nodeAccessType) {
    }

    @Override
    public TM createRecord() {
        return DependencyResolver.resolve(modelType);
    }

    @Override
    public void addRecord(TM record) {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            TD data;
            try {
                data = dataType.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(RecordModelEditableCollection.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            updateData(data, record);
            domainModel.create(data);
            changeTracker.addChange(dataType, data, EntityChangeType.ADDED, domainModel);
            domainModel.completeTransaction();
            record.setId(data.getId());
        }
    }

    @Override
    public void updateRecord(TM record) {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            TD data = domainModel.query().from(dataType).find(dataType, record.getId());
            updateData(data, record);
            domainModel.update(data);
            changeTracker.addChange(dataType, data, EntityChangeType.UPDATED, domainModel);
            domainModel.completeTransaction();
        }
    }

    @Override
    public boolean deleteRecords(Collection<Long> recordIds) {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            for (long id : recordIds) {
                domainModel.delete(dataType, id);
                changeTracker.addChange(dataType, id, EntityChangeType.REMOVED, domainModel);
            }
            domainModel.completeTransaction();
        }
        return true;
    }
}
