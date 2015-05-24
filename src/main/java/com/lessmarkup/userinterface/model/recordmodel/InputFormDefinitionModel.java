package com.lessmarkup.userinterface.model.recordmodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.recordmodel.InputFieldDefinition;
import com.lessmarkup.interfaces.recordmodel.InputFieldEnum;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.InputSource;
import com.lessmarkup.interfaces.recordmodel.RecordModelCache;
import com.lessmarkup.interfaces.recordmodel.RecordModelDefinition;
import com.lessmarkup.interfaces.recordmodel.SelectValueModel;
import com.lessmarkup.interfaces.system.EngineConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InputFormDefinitionModel {
    
    private final DataCache dataCache;
    private final List<InputFieldModel> fields = new ArrayList<>();
    private String title;
    private boolean submitWithCaptcha;
    
    @Inject
    public InputFormDefinitionModel(DataCache dataCache) {
        this.dataCache = dataCache;
    }
    
    public void initialize(String id) {
        RecordModelCache recordModelCache = this.dataCache.get(RecordModelCache.class);
        RecordModelDefinition definition = recordModelCache.getDefinition(id).get();
        initialize(definition);
    }
    
    public void initialize(Class<?> type) {
        RecordModelCache recordModelCache = this.dataCache.get(RecordModelCache.class);
        RecordModelDefinition definition = recordModelCache.getDefinition(type).get();
        initialize(definition);
    }

    public Collection<InputFieldModel> getFields() {
        return this.fields;
    }
    
    private void initialize(RecordModelDefinition definition) {
        if (definition == null) {
            return;
        }

        this.title = LanguageHelper.getText(definition.getModuleType(), definition.getTitleTextId());

        if (definition.isSubmitWithCaptcha()) {
            EngineConfiguration engineConfiguration = RequestContextHolder.getContext().getEngineConfiguration();
            String privateKey = engineConfiguration.getRecaptchaPrivateKey();
            String publicKey = engineConfiguration.getRecaptchaPublicKey();
            if (privateKey != null && privateKey.length() > 0 && publicKey != null && publicKey.length() > 0) {
                this.submitWithCaptcha = true;
            }
        }
        
        InputSource inputSource = null;
        
        for (InputFieldDefinition source : definition.getFieldsJava()) {
            InputFieldModel target = new InputFieldModel(source, definition);
            
            if (source.getEnumValues() != null && source.getEnumValues().size() > 0) {
                for (InputFieldEnum value : source.getEnumValues()) {
                    target.getSelectValues().add(new SelectValueModel(LanguageHelper.getFullTextId(definition.getModuleType(), value.getTextId()), value.getValue()));
                }
            } else if (source.getType() == InputFieldType.SELECT || source.getType() == InputFieldType.SELECT_TEXT || source.getType() == InputFieldType.MULTI_SELECT) {
                if (inputSource == null && InputSource.class.isAssignableFrom(definition.getModelType())) {
                    inputSource = (InputSource) DependencyResolver.resolve(definition.getModelType());
                }
                
                if (inputSource != null) {
                    inputSource.getEnumValues(source.getProperty().getName()).forEach(es -> target.getSelectValues().add(new SelectValueModel(es.getText(), es.getValue())));
                }
            }
            
            this.fields.add(target);
        }
    }
    
    public JsonElement toJson() {

        JsonObject ret = new JsonObject();

        ret.addProperty("title", this.title);
        ret.addProperty("submitWithCaptcha", this.submitWithCaptcha);

        JsonArray fieldsArray = new JsonArray();

        for (InputFieldModel model : fields) {
            fieldsArray.add(model.toJson());
        }

        ret.add("fields", fieldsArray);
        
        return ret;
    }
}
