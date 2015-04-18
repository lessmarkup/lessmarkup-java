/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.framework.data;

import com.lessmarkup.interfaces.cache.AbstractCacheHandler;
import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.structure.Tuple;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalLong;

@Component
@Scope("prototype")
public class EditableCollectionCache extends AbstractCacheHandler {

    private final Map<Tuple<Class<? extends RecordModel>, Class<? extends DataObject>>, List<RecordToDataPropertyMapper>> propertySets = new HashMap<>();
    
    public EditableCollectionCache() {
        super(null);
    }
    
    @Override
    public void initialize(OptionalLong objectId) {
        if (objectId.isPresent()) {
            throw new IllegalArgumentException();
        }
    }
    
    private static Map<String, Method> getTypeMethods(Class<?> type) {
        Map<String, Method> methods = new HashMap<>();
        for (Method method : type.getMethods()) {
            if ((method.getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }
            methods.put(method.getName(), method);
        }
        return methods;
    }
    
    synchronized List<RecordToDataPropertyMapper> getProperties(Class<? extends RecordModel> modelType, Class<? extends DataObject> dataType) {
        
        Tuple<Class<? extends RecordModel>, Class<? extends DataObject>> key = new Tuple(modelType, dataType);
        
        List<RecordToDataPropertyMapper> ret = propertySets.get(key);
        if (ret != null) {
            return ret;
        }
        
        ret = new ArrayList<>();
        
        Map<String, Method> modelMethods = getTypeMethods(modelType);
        Map<String, Method> dataMethods = getTypeMethods(dataType);
        
        for (Entry<String, Method> entry : modelMethods.entrySet()) {
            if (!entry.getKey().startsWith("set")) {
                continue;
            }
            
            String setterName = entry.getKey();
            Method modelSetter = entry.getValue();
            
            if (modelSetter.getParameterCount() != 1) {
                continue;
            }
            
            Class<?> fieldType = modelSetter.getParameterTypes()[0];
            
            String getterName;
            
            if (fieldType.equals(boolean.class)) {
                getterName = "is" + setterName.substring(3);
            } else {
                getterName = "get" + setterName.substring(3);
            }
            
            Method modelGetter = modelMethods.get(getterName);
            if (modelGetter == null || modelGetter.getParameterCount() != 0 || !modelGetter.getReturnType().equals(fieldType)) {
                continue;
            }
            
            Method dataSetter = dataMethods.get(setterName);
            if (dataSetter == null || dataSetter.getParameterCount() != 1 || !dataSetter.getParameterTypes()[0].equals(fieldType)) {
                continue;
            }
            
            Method dataGetter = dataMethods.get(getterName);
            if (dataGetter == null || dataGetter.getParameterCount() != 0 || !dataGetter.getReturnType().equals(fieldType)) {
                continue;
            }
            
            ret.add(new RecordToDataPropertyMapper(dataGetter, dataSetter, modelGetter, modelSetter, fieldType));
        }
        
        propertySets.put(key, ret);
        
        return ret;
    }
}
