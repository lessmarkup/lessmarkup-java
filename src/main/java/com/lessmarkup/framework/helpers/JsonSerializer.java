package com.lessmarkup.framework.helpers;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.lessmarkup.interfaces.exceptions.CommonException;
import java.time.OffsetDateTime;
import java.util.OptionalLong;

public final class JsonSerializer {
    
    public static <T> T deserializePojo(Class<T> type, String data) {
        return deserializePojo(type, deserializeToTree(data));
    }
    
    public static JsonElement serializePojoToTree(Object object) {
        
        Class<?> type = object.getClass();
        if (type.equals(long.class)) {
            return new JsonPrimitive((long) object);
        } else if (type.equals(int.class)) {
            return new JsonPrimitive((int) object);
        } else if (type.equals(String.class)) {
            return new JsonPrimitive((String) object);
        } else if (JsonElement.class.isAssignableFrom(type)) {
            return (JsonElement) object;
        }
        
        JsonObject ret = new JsonObject();
        for (PropertyDescriptor property : TypeHelper.getProperties(object.getClass())) {
            if (property.getType().equals(long.class)) {
                ret.addProperty(property.getName(), (long) property.getValue(object));
            } else if (property.getType().equals(String.class)) {
                ret.addProperty(property.getName(), (String) property.getValue(object));
            } else if (property.getType().equals(boolean.class)) {
                ret.addProperty(property.getName(), (boolean) property.getValue(object));
            } else if (property.getType().equals(int.class)) {
                ret.addProperty(property.getName(), (int) property.getValue(object));
            } else if (property.getType().equals(OptionalLong.class)) {
                OptionalLong value = (OptionalLong) property.getValue(object);
                if (value.isPresent()) {
                    ret.addProperty(property.getName(), value.getAsLong());
                } else {
                    ret.add(property.getName(), JsonNull.INSTANCE);
                }
            } else if (property.getType().equals(OffsetDateTime.class)) {
                ret.addProperty(property.getName(), ((OffsetDateTime)property.getValue(object)).toString());
            } else if (JsonElement.class.isAssignableFrom(type)) {
                ret.add(property.getName(), (JsonElement) property.getValue(object));
            }
        }
        return ret;
    }
    
    public static String serializePojo(Object object) {
        JsonElement obj = serializePojoToTree(object);
        return obj.toString();
    }

    public static JsonElement deserializeToTree(String data) {
        return new JsonParser().parse(data);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializePojo(Class<T> type, JsonElement data) {
        
        if (type.equals(int.class)) {
            return (T)(Integer) data.getAsInt();
        } else if (type.equals(long.class)) {
            return (T)(Long) data.getAsLong();
        } else if (type.equals(String.class)) {
            return (T) data.getAsString();
        } else if (type.equals(OptionalLong.class)) {
            if (data.isJsonNull()) {
                return (T) OptionalLong.empty();
            } else {
                return (T) OptionalLong.of(data.getAsLong());
            }
        } else if (JsonElement.class.isAssignableFrom(type)) {
            return (T) data;
        }
        
        if (data.isJsonNull() || !data.isJsonObject()) {
            return null;
        }
        
        JsonObject object = data.getAsJsonObject();
        
        T ret;
        
        try {
            ret = type.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new CommonException(ex);
        }
        
        for (PropertyDescriptor property : TypeHelper.getProperties(type)) {
            JsonElement element = object.get(property.getName());
            if (element == null) {
                continue;
            }
            
            if (property.getType().equals(long.class)) {
                property.setValue(ret, element.getAsLong());
            } else if (property.getType().equals(String.class)) {
                property.setValue(ret, element.getAsString());
            } else if (property.getType().equals(boolean.class)) {
                property.setValue(ret, element.getAsBoolean());
            } else if (property.getType().equals(int.class)) {
                property.setValue(ret, element.getAsInt());
            } else if (property.getType().equals(OptionalLong.class)) {
                if (!element.isJsonNull()) {
                    property.setValue(ret, element.getAsLong());
                }
            } else if (property.getType().equals(OffsetDateTime.class)) {
                if (!element.isJsonNull()) {
                    property.setValue(ret, OffsetDateTime.parse(element.getAsString()));
                }
            } else if (JsonElement.class.isAssignableFrom(property.getType())) {
                property.setValue(ret, element);
            }
        }
        
        return ret;
    }
}
