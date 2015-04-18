/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lessmarkup.framework.helpers;

/*import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;*/
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author User
 */
public final class JsonSerializer {
    
    public static <T> T deserialize(Class<T> type, String data) {
        Gson gson = new Gson();
        return gson.fromJson(data, type);
    }
    
    public static String serialize(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }
    
    public static JsonElement serializeToTree(Object object) {
        Gson gson = new Gson();
        return gson.toJsonTree(object);
    }

    public static JsonObject deserialize(String data) {
        JsonElement element = new JsonParser().parse(data);
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return null;
    }
    
    public static <T> T deserialize(Class<T> type, JsonElement data) {
        
        Gson gson = new Gson();
        return gson.fromJson(data, type);
        
        /*switch (data.getValueType()) {
            case NUMBER:
                JsonNumber number = (JsonNumber) data;
                if (type.equals(Integer.class)) {
                    return (T)(Integer) number.intValue();
                } else if (type.equals(Double.class)) {
                    return (T)(Double) number.doubleValue();
                } else if (type.equals(Long.class)) {
                    return (T)(Long) number.longValue();
                }
                break;
            case STRING:
                if (type.equals(String.class)) {
                    JsonString string = (JsonString) data;
                    return (T) string.getString();
                }
                break;
            case NULL:
                return null;
            case FALSE:
                if (type.equals(Boolean.class)) {
                    return (T) (Boolean) false;
                }
                break;
            case TRUE:
                if (type.equals(Boolean.class)) {
                    return (T) (Boolean) true;
                }
                break;
            case ARRAY:
                JsonArray array = (JsonArray) data;
                Type superclass = type.getGenericSuperclass();
                if (superclass == null) {
                    return null;
                }
                if (!superclass.getClass().equals(List.class)) {
                    return null;
                }
                Type[] parameters = ((ParameterizedType)type.getGenericSuperclass()).getActualTypeArguments();
                if (parameters.length != 1) {
                    return null;
                }
                Class<?> arrayType = parameters[0].getClass();
                Object arrayInstance;
                try {
                    arrayInstance = type.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    Logger.getLogger(JsonSerializer.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
                Method addMethod = Arrays.stream(type.getMethods()).filter(m -> "add".equals(m.getName())).findFirst().get();
                for (JsonValue item : array) {
                    Object itemObject = deserialize(arrayType, item);
                    try {
                        addMethod.invoke(arrayInstance, itemObject);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(JsonSerializer.class.getName()).log(Level.SEVERE, null, ex);
                        return null;
                    }
                }
                return (T) arrayInstance;
        }
        
        if (data.getValueType() != JsonValue.ValueType.OBJECT) {
            return null;
        }
        
        JsonObject objectData = (JsonObject) data;
        
        Object instance;
        
        try {
            instance = type.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(JsonSerializer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        for (Method method : type.getMethods()) {
            if (!method.getName().startsWith("set")) {
                continue;
            }
            Type[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                continue;
            }
            JsonValue property = objectData.get(method.getName().substring(3));
            if (property == null) {
                continue;
            }
            Object propertyObject = deserialize(parameterTypes[0].getClass(), property);
            try {
                method.invoke(instance, propertyObject);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(JsonSerializer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        
        return (T) instance;*/
    }
}
