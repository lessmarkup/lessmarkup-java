package com.lessmarkup.userinterface.nodehandlers.common;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.recordmodel.RecordModelCache;
import com.lessmarkup.interfaces.recordmodel.RecordModelDefinition;
import com.lessmarkup.interfaces.structure.ActionAccess;
import com.lessmarkup.interfaces.structure.Parameter;
import com.lessmarkup.userinterface.model.recordmodel.InputFieldModel;
import com.lessmarkup.userinterface.model.recordmodel.InputFormDefinitionModel;

public abstract class DialogNodeHandler<T> extends AbstractNodeHandler {

    private final InputFormDefinitionModel definitionModel;
    private final DataCache dataCache;
    private final Class<T> modelType;

    protected DialogNodeHandler(DataCache dataCache, Class<T> modelType) {
        this.dataCache = dataCache;
        this.definitionModel = DependencyResolver.resolve(InputFormDefinitionModel.class);
        this.modelType = modelType;
        definitionModel.initialize(modelType);

        for (InputFieldModel field : definitionModel.getFields()) {
            switch (field.getType()) {
                case CODE_TEXT:
                    addScript("scripts/directives/CodeMirrorDirective");
                    break;
                case RICH_TEXT:
                    addScript("scripts/directives/CkEditorDirective");
                    break;
            }
        }
    }

    protected abstract T loadObject();

    protected abstract String saveObject(T changedObject);

    protected String getApplyCaption() {
        return LanguageHelper.getFullTextId(Constants.ModuleType.MAIN, TextIds.APPLY_BUTTON);
    }

    @Override
    public JsonObject getViewData() {
        JsonObject ret = new JsonObject();
        ret.add("definition", definitionModel.toJson());
        Object source = loadObject();
        if (source == null) {
            ret.add("object", JsonNull.INSTANCE);
        } else {
            ret.add("object", JsonSerializer.serializePojoToTree(source));
        }
        ret.addProperty("applyCaption", getApplyCaption());
        return ret;
    }

    @ActionAccess
    public JsonObject save(@Parameter("changedObject") JsonObject changedObject) {
        RecordModelDefinition model = dataCache.get(RecordModelCache.class).getDefinition(modelType);
        model.validateInput(changedObject, false);
        String message = saveObject(JsonSerializer.deserializePojo(modelType, changedObject));
        if (message == null) {
            message = LanguageHelper.getText(Constants.ModuleType.MAIN, TextIds.SUCCESSFULLY_SAVED);
        }
        JsonObject ret = new JsonObject();
        ret.addProperty("message", message);
        return ret;
    }

    @Override
    public String getViewType() {
        return "dialog";
    }
}
