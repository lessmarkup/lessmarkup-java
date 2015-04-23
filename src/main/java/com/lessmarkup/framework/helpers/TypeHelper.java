package com.lessmarkup.framework.helpers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class TypeHelper {
    
    private static final Map<Class<?>, Collection<PropertyDescriptor>> propertyMap = new HashMap<>();

    public synchronized static Iterable<PropertyDescriptor> getProperties(Class<?> type) {
        
        Collection<PropertyDescriptor> ret = propertyMap.get(type);
        if (ret != null) {
            return ret;
        }
        
        ret = new ArrayList<>();
        propertyMap.put(type, ret);

        Map<String, Method> methods = new HashMap<>();

        for (Method method : type.getMethods()) {
            if ((method.getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }
            methods.put(method.getName(), method);
        }

        for (Map.Entry<String, Method> entry : methods.entrySet()) {
            if (!entry.getKey().startsWith("set")) {
                continue;
            }

            Method setter = entry.getValue();

            if (setter.getParameterCount() != 1) {
                continue;
            }

            Class<?> fieldType = setter.getParameterTypes()[0];

            String propertyName = entry.getKey().substring(3);
            String getterName;

            if (boolean.class.equals(fieldType)) {
                getterName = "is" + propertyName;
            } else {
                getterName = "get" + propertyName;
            }

            Method getter = methods.get(getterName);

            if (getter == null) {
                continue;
            }

            ret.add(new PropertyDescriptor(StringHelper.toJsonCase(propertyName), getter, setter, fieldType));
        }

        return ret;
    }
}
