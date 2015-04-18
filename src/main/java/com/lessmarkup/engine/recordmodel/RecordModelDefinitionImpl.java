package com.lessmarkup.engine.recordmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.engine.scripting.ScriptHelper;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.PropertyDescriptor;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.helpers.TypeHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.exceptions.RecordValidationException;
import com.lessmarkup.interfaces.recordmodel.*;
import com.lessmarkup.interfaces.system.EngineConfiguration;

import java.util.*;
import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class RecordModelDefinitionImpl implements RecordModelDefinition {

    private String titleTextId;
    private String moduleType;
    private Class<? extends RecordModel> modelType;
    private Class<? extends AbstractDataObject> dataType;
    private Class<? extends ModelCollection> collectionType;
    private boolean submitWithCaptcha;
    private final List<InputFieldDefinition> fields = new ArrayList<>();
    private final List<RecordColumnDefinition> columns = new ArrayList<>();
    private String id;
    
    public void initialize(Class<? extends RecordModel> modelType, String moduleType) {
        
        RecordModel recordModel = DependencyResolver.resolve(modelType);
        
        this.titleTextId = recordModel.getTitleTextId();
        this.moduleType = moduleType;
        this.modelType = modelType;
        this.dataType = recordModel.getDataType();
        this.collectionType = recordModel.getCollectionType();
        this.submitWithCaptcha = recordModel.getSubmitWithCaptcha();

        Collection<PropertyDescriptor> properties = TypeHelper.getProperties(modelType);

        for (PropertyDescriptor property : properties) {
            InputField inputField = property.getAnnotation(InputField.class);

            if (inputField != null) {
                InputFieldDefinition definition = new InputFieldDefinition(property, inputField);
                this.fields.add(definition);
            }
            
            RecordColumn recordColumn = property.getAnnotation(RecordColumn.class);

            if (recordColumn != null) {
                RecordColumnDefinition definition = new RecordColumnDefinition();
                definition.initialize(recordColumn, property);
                this.columns.add(definition);
                
                if (recordColumn.cellTemplate() != null) {
                    definition.setCellTemplate(recordColumn.cellTemplate());
                }
            }
        }
    }
    
    @Override
    public Class<? extends ModelCollection> getCollectionType() {
        return this.collectionType;
    }

    @Override
    public String getTitleTextId() {
        return this.titleTextId;
    }

    @Override
    public String getModuleType() {
        return this.moduleType;
    }

    @Override
    public Class<?> getModelType() {
        return this.modelType;
    }

    @Override
    public Class<?> getDataType() {
        return this.dataType;
    }

    @Override
    public String getId() {
        return this.id;
    }
    
    void setId(String id) {
        this.id = id;
    }

    @Override
    public List<InputFieldDefinition> getFields() {
        return this.fields;
    }

    @Override
    public List<RecordColumnDefinition> getColumns() {
        return this.columns;
    }
    
    private final String ChallengeFieldKey = "-RecaptchaChallenge-";
    private final String ResponseFieldKey = "-RecaptchaResponse-";

    @Override
    public void validateInput(JsonElement objectToValidate, boolean isNew, String properties) throws RecordValidationException {
        if (this.submitWithCaptcha) {
            if (properties == null || properties.length() == 0) {
                throw new RecordValidationException("Cannot validate captcha");
            }
            
            JsonParser parser = new JsonParser(); 
            JsonElement propertiesElement = parser.parse(properties);
            
            if (!propertiesElement.isJsonObject()) {
                throw new RecordValidationException("Cannot validate captcha");
            }
            
            JsonObject propertiesObject = propertiesElement.getAsJsonObject();
            
            String challengeValue = propertiesObject.getAsJsonPrimitive(ChallengeFieldKey).toString();
            String responseValue = propertiesObject.getAsJsonPrimitive(ResponseFieldKey).toString();

            EngineConfiguration engineConfiguration = RequestContextHolder.getContext().getEngineConfiguration();
            ReCaptcha reCaptcha = ReCaptchaFactory.newReCaptcha(engineConfiguration.getRecaptchaPublicKey(), engineConfiguration.getRecaptchaPrivateKey(), false);
            ReCaptchaResponse response = reCaptcha.checkAnswer(RequestContextHolder.getContext().getRemoteAddress(), challengeValue, responseValue);
            
            if (!response.isValid()) {
                throw new RecordValidationException(response.getErrorMessage());
            }
        }
        
        for (InputFieldDefinition field : this.fields) {
            if (!field.isRequired()) {
                continue;
            }
            
            if (field.getType() == InputFieldType.FILE && !isNew) {
                continue;
            }
            
            if (!StringHelper.isNullOrEmpty(field.getVisibleCondition()) && !ScriptHelper.evaluateExpression(field.getVisibleCondition(), objectToValidate)) {
                continue;
            }
            
            if (!StringHelper.isNullOrEmpty(field.getReadOnlyCondition()) && ScriptHelper.evaluateExpression(field.getReadOnlyCondition(), objectToValidate)) {
                continue;
            }
            
            if (!objectToValidate.isJsonObject()) {
                continue;
            }
            
            JsonElement fieldValue = objectToValidate.getAsJsonObject().get(StringHelper.toJsonCase(field.getProperty().getName()));
            
            if (fieldValue != null && fieldValue.toString().length() > 0) {
                continue;
            }
            
            String errorText = LanguageHelper.getText(Constants.ModuleType.MAIN, TextIds.PROPERTY_MUST_BE_SPECIFIED);
            String fieldText = field.getTextId() == null ? "" : LanguageHelper.getText(moduleType, field.getTextId());
            
            throw new RecordValidationException(String.format(errorText, fieldText));
        }
    }

    @Override
    public boolean isSubmitWithCaptcha() {
        return this.submitWithCaptcha;
    }
}
