/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.framework.data;

import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class RecordToDataPropertyMapper {
    private final Method collectionGetter, collectionSetter, modelGetter, modelSetter;
    private final Class<?> fieldType;
    
    public RecordToDataPropertyMapper(Method collectionGetter, Method collectionSetter, Method modelGetter, Method modelSetter, Class<?> fieldType) {
        this.collectionGetter = collectionGetter;
        this.collectionSetter = collectionSetter;
        this.modelGetter = modelGetter;
        this.modelSetter = modelSetter;
        this.fieldType = fieldType;
    }
    
    public <T extends RecordModel> Object getModelValue(T thisValue) {
        try {
            return this.modelGetter.invoke(thisValue);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new CommonException(ex);
        }
    }
    
    public <T extends RecordModel> void setModelValue(T thisValue, Object newValue) {
        try {
            this.modelSetter.invoke(thisValue, newValue);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new CommonException(ex);
        }
    }
    
    public <T extends DataObject> Object getDataValue(T thisValue) {
        try {
            return this.collectionGetter.invoke(thisValue);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new CommonException(ex);
        }
    }

    public <T extends DataObject> void setDataValue(T thisValue, Object newValue) {
        try {
            this.collectionSetter.invoke(thisValue, newValue);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new CommonException(ex);
        }
    }
    
    public Class<?> getFieldType() { return fieldType; }
}
