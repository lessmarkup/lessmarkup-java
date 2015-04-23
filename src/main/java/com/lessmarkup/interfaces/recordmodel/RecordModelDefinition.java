package com.lessmarkup.interfaces.recordmodel;

import com.google.gson.JsonElement;
import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.exceptions.RecordValidationException;
import java.util.List;

public interface RecordModelDefinition {
    String getTitleTextId();
    String getModuleType();
    Class<? extends RecordModel> getModelType();
    Class<? extends DataObject> getDataType();
    String getId();
    List<InputFieldDefinition> getFields();
    List<RecordColumnDefinition> getColumns();
    void validateInput(JsonElement objectToValidate, boolean isNew) throws RecordValidationException;
    boolean isSubmitWithCaptcha();
    ModelCollection createModelCollection();
}
