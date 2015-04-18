package com.lessmarkup.engine.recordmodel;

import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.interfaces.cache.AbstractCacheHandler;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.recordmodel.RecordModelCache;
import com.lessmarkup.interfaces.recordmodel.RecordModelDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.OptionalLong;
import java.util.logging.Level;

@Component
@Scope("prototype")
public class RecordModelCacheImpl extends AbstractCacheHandler implements RecordModelCache {

    private final ModuleProvider moduleProvider;
    
    private final HashMap<Class<?>, RecordModelDefinition> definitions = new HashMap<>();
    private final HashMap<String, RecordModelDefinition> idToDefinition = new HashMap<>();
    private final HashMap<Class<?>, String> typeToId = new HashMap<>();

    @Autowired
    public RecordModelCacheImpl(ModuleProvider moduleProvider) {
        super(new Class<?>[] { com.lessmarkup.dataobjects.Language.class });
        this.moduleProvider = moduleProvider;
    }
    
    private String createDefinitionId(RecordModelDefinition definition, int i, MessageDigest messageDigest) {
        String idString = definition.getModelType().getName() + i;
        
        byte[] bytes = messageDigest.digest(idString.getBytes());
        
        idString = Base64.getEncoder().encodeToString(bytes);
        
        return idString;
    }

    @Override
    public void initialize(OptionalLong objectId) {
        MessageDigest messageDigest;
        
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, ex);
            return;
        }
        
        for (ModuleConfiguration module : this.moduleProvider.getModules()) {
            Collection<Class<? extends RecordModel>> modelTypes = module.getInitializer().getModelTypes();
            if (modelTypes == null) {
                continue;
            }
            for (Class<? extends RecordModel> type : modelTypes) {
                RecordModelDefinitionImpl definition = DependencyResolver.resolve(RecordModelDefinitionImpl.class);
                definition.initialize(type, module.getModuleType());
                
                String definitionId;
                
                for (int i = 0;; i++) {
                    definitionId = createDefinitionId(definition, i, messageDigest);
                    if (!this.idToDefinition.containsKey(definitionId)) {
                        break;
                    }
                }
                
                definition.setId(definitionId);
                this.idToDefinition.put(definitionId, definition);
                this.definitions.put(type, definition);
                this.typeToId.put(type, definitionId);
            }
        }
    }
    
    @Override
    public RecordModelDefinition getDefinition(Class<?> type) {
        return this.definitions.get(type);
    }

    @Override
    public RecordModelDefinition getDefinition(String id) {
        return this.idToDefinition.get(id);
    }

    @Override
    public boolean hasDefinition(Class<?> type) {
        return this.definitions.containsKey(type);
    }
}
