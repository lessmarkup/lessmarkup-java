package com.lessmarkup.framework.helpers;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DependencyResolver {

    private final ClassPathXmlApplicationContext context;

    private DependencyResolver() {
        context = new ClassPathXmlApplicationContext("main/applicationContext.xml");
    }

    private static class LazyHolder {
        private static final DependencyResolver INSTANCE = new DependencyResolver();
    }

    public static <T> T resolve(Class<T> type) {
        DependencyResolver instance = LazyHolder.INSTANCE;
        return (T) instance.context.getBean(type);
    }
/*

    private static final Object instanceSyncObject = new Object();
    
    private final Map<Class<?>, Object> singletonMap = new HashMap<>();
    private final Map<Class<?>, Class<?>> typeMap = new HashMap<>();
    
    private Object constructObject(Constructor[] constructors) {
        for (Constructor constructor : constructors) {
            
            List<Object> parameters = new ArrayList<>();
            
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                Object parameterValue = resolveInternal(parameterType);
                if (parameterValue == null) {
                    break;
                }
                parameters.add(parameterValue);
            }
            
            try {
                return constructor.newInstance(parameters);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(DependencyResolver.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        
        return null;
    }
    
    private Object resolveInternal(Class<?> type) {
        Class<?> mappedType = typeMap.get(type);
        if (mappedType != null) {
            type = mappedType;
        }        
        
        synchronized(instanceSyncObject) {
            if (singletonMap.containsKey(type)) {
                return singletonMap.get(type);
            }
        }
        
        if (type.isAnnotationPresent(Singleton.class)) {
            synchronized(instanceSyncObject) {
                Object singletonInstance = constructObject(type.getConstructors());
                singletonMap.put(type, singletonInstance);
                return singletonInstance;
            }
        }
        
        return constructObject(type.getConstructors());
    }
    
    public static <T> T resolve(Class<T> type) {
        return (T) instance.resolveInternal(type);
    }
    
    public static <T> void registerSingleton(Class<T> type, T instance) {
        DependencyResolver.instance.singletonMap.put(type, instance);
    }
    
    public static <T, E extends T> void registerMapping(Class<T> source, Class<E> target) {
        DependencyResolver.instance.typeMap.put(source, target);
    }*/
}
