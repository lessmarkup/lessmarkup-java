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
                    addScript("lib/codemirror/codemirror");
                    addScript("lib/codemirror/plugins/css");
                    addScript("lib/codemirror/plugins/css-hint");
                    addScript("lib/codemirror/plugins/dialog");
                    addScript("lib/codemirror/plugins/anyword-hint");
                    addScript("lib/codemirror/plugins/brace-fold");
                    addScript("lib/codemirror/plugins/closebrackets");
                    addScript("lib/codemirror/plugins/closetag");
                    addScript("lib/codemirror/plugins/colorize");
                    addScript("lib/codemirror/plugins/comment");
                    addScript("lib/codemirror/plugins/comment-fold");
                    addScript("lib/codemirror/plugins/continuecomment");
                    addScript("lib/codemirror/plugins/foldcode");
                    addScript("lib/codemirror/plugins/fullscreen");
                    addScript("lib/codemirror/plugins/html-hint");
                    addScript("lib/codemirror/plugins/htmlembedded");
                    addScript("lib/codemirror/plugins/htmlmixed");
                    addScript("lib/codemirror/plugins/indent-fold");
                    addScript("lib/codemirror/plugins/javascript");
                    addScript("lib/codemirror/plugins/javascript-hint");
                    addScript("lib/codemirror/plugins/mark-selection");
                    addScript("lib/codemirror/plugins/markdown-fold");
                    addScript("lib/codemirror/plugins/match-highlighter");
                    addScript("lib/codemirror/plugins/matchbrackets");
                    addScript("lib/codemirror/plugins/matchtags");
                    addScript("lib/codemirror/plugins/placeholder");
                    addScript("lib/codemirror/plugins/rulers");
                    addScript("lib/codemirror/plugins/scrollpastend");
                    addScript("lib/codemirror/plugins/search");
                    addScript("lib/codemirror/plugins/searchcursor");
                    addScript("lib/codemirror/plugins/xml");
                    addScript("lib/codemirror/plugins/xml-fold");
                    addScript("lib/codemirror/plugins/xml-hint");
                    addScript("lib/codemirror/ui-codemirror");
                    break;
                case RICH_TEXT:
                    addScript("lib/ckeditor/ckeditor");
                    addScript("directives/angular-ckeditor");
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
