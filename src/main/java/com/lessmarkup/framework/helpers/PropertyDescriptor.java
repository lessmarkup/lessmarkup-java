package com.lessmarkup.framework.helpers;

import com.lessmarkup.interfaces.exceptions.CommonException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyDescriptor {
    private final Method getter;
    private final Method setter;
    private final String name;
    private final Class<?> type;
    
    public PropertyDescriptor(String name, Method getter, Method setter, Class<?> type) {
        this.getter = getter;
        this.setter = setter;
        this.name = name;
        this.type = type;
    }

    public Object getValue(Object thisObject) {
        try {
            return this.getter.invoke(thisObject);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new CommonException(ex);
        } catch (InvocationTargetException ex) {
            throw new CommonException(ex.getTargetException());
        }
    }
    
    public void setValue(Object thisObject, Object newValue) {
        try {
            this.setter.invoke(thisObject, newValue);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new CommonException(ex);
        } catch (InvocationTargetException ex) {
            throw new CommonException(ex.getTargetException());
        }
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T ret = this.getter.getAnnotation(annotationClass);
        if (ret != null) {
            return ret;
        }
        return this.setter.getAnnotation(annotationClass);
    }
}
