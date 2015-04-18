package com.lessmarkup.interfaces.recordmodel;

import com.google.gson.JsonElement;
import com.lessmarkup.interfaces.exceptions.RecordValidationException;
import java.util.List;

public interface RecordModelDefinition {
    Class<? extends ModelCollection> getCollectionType();
    String getTitleTextId();
    String getModuleType();
    Class<?> getModelType();
    Class<?> getDataType();
    String getId();
    List<InputFieldDefinition> getFields();
    List<RecordColumnDefinition> getColumns();
    void validateInput(JsonElement objectToValidate, boolean isNew, String properties) throws RecordValidationException;
    boolean isSubmitWithCaptcha();
}
