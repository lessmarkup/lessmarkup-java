package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.structure.ActionAccess;
import com.lessmarkup.interfaces.structure.NodeCache;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.structure.Tuple;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ExecuteActionModel {
    
    private final DataCache dataCache;
    
    @Autowired
    public ExecuteActionModel(DataCache dataCache) {
        this.dataCache = dataCache;
    }
    
    public JsonObject handleRequest(JsonObject data, String path) {
        if (path != null) {
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(ExecuteActionModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (path != null) {
            int queryPost = path.indexOf('?');
            if (queryPost >= 0) {
                path = path.substring(0, queryPost);
            }
        }
        
        NodeCache nodeCache = this.dataCache.get(NodeCache.class);
        NodeHandler handler = nodeCache.getNodeHandler(path);
        
        String actionName = data.get("command").getAsString();
        
        Tuple<Object, Method> actionMethod = handler.getActionHandler(actionName, data);
        
        if (actionMethod == null) {
            throw new IllegalArgumentException();
        }
        
        ActionAccess actionAccess = actionMethod.getValue2().getAnnotation(ActionAccess.class);
        
        if (actionAccess == null || handler.getAccessType().getLevel() < actionAccess.minimumAccess().getLevel()) {
            throw new IllegalAccessError();
        }

        Parameter[] parameters = actionMethod.getValue2().getParameters();
        
        Object[] arguments = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {

            Parameter parameter = parameters[i];
            String parameterName;
            com.lessmarkup.interfaces.structure.Parameter attribute = parameter.getAnnotation(com.lessmarkup.interfaces.structure.Parameter.class);
            if (attribute != null) {
                parameterName = attribute.value();
            } else {
                parameterName = StringHelper.toJsonCase(parameters[i].getName());
            }
            Class<?> parameterType = parameters[i].getType();
            
            JsonElement dataParameter = data.get(parameterName);
            
            if (dataParameter == null) {
                if (parameterName.startsWith("raw") && parameterType.equals(String.class)) {
                    dataParameter = data.get(StringHelper.toJsonCase(parameterName.substring(3)));
                    if (dataParameter != null) {
                        arguments[i] = dataParameter.toString();
                    }
                }
                continue;
            }
            
            arguments[i] = JsonSerializer.deserialize(parameterType, dataParameter);
        }
        
        try {
            return (JsonObject) actionMethod.getValue2().invoke(actionMethod.getValue1(), arguments);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new CommonException(ex);
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof CommonException) {
                throw (CommonException) targetException;
            }
            throw new CommonException(targetException);
        }
    }
}
