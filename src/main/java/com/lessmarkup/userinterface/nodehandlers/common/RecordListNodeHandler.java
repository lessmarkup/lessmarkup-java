package com.lessmarkup.userinterface.nodehandlers.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.*;
import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.*;
import com.lessmarkup.interfaces.recordmodel.*;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import com.lessmarkup.interfaces.structure.RecordAction;
import com.lessmarkup.interfaces.system.LanguageCache;
import com.lessmarkup.interfaces.system.ResourceCache;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import com.lessmarkup.interfaces.system.UserCache;
import com.lessmarkup.interfaces.text.SupportsTextSearch;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class RecordListNodeHandler<T extends RecordModel> extends AbstractNodeHandler {
    static class Link {
        private String text;
        private String url;
        private boolean external;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isExternal() {
            return external;
        }

        public void setExternal(boolean external) {
            this.external = external;
        }
    }

    static enum ActionType {
        RECORD, // Is assigned to each record
        CREATE, // To show new type create dialog (like new record or new forum thread etc)
        RECORD_CREATE, // To show new type create dialog associated with existing record
        RECORD_INITIALIZE_CREATE, // To show new type create dialog associated with existing record, with pre-initialization
    }

    static class Action {
        private String text;
        private String name;
        private String visible;
        private String parameter;
        private ActionType type;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVisible() {
            return visible;
        }

        public void setVisible(String visible) {
            this.visible = visible;
        }

        public String getParameter() {
            return parameter;
        }

        public void setParameter(String parameter) {
            this.parameter = parameter;
        }

        public ActionType getType() {
            return type;
        }

        public void setType(ActionType type) {
            this.type = type;
        }
    }

    private final Map<String, String> columnUrls = new HashMap<>();
    private final List<Link> links = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();
    private final Collection<PropertyDescriptor> allProperties;
    private final PropertyDescriptor idProperty;
    private ModelCollection<T> collection;
    private EditableModelCollection<T> editableCollection;
    private final RecordModelDefinition recordModel;
    private final DataCache dataCache;
    private final DomainModelProvider domainModelProvider;
    private final Class<T> modelType;

    protected PropertyDescriptor getIdProperty() { return idProperty; }

    protected RecordModelDefinition getRecordModel() { return recordModel; }

    protected RecordListNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache, Class<T> modelType) {
        this.domainModelProvider = domainModelProvider;
        this.dataCache = dataCache;
        this.modelType = modelType;

        RecordModelCache formCache = dataCache.get(RecordModelCache.class);
        recordModel = formCache.getDefinition(modelType);

        if (recordModel == null) {
            throw new IllegalArgumentException(LanguageHelper.getText(Constants.ModuleType.MAIN, TextIds.MISSING_PARAMETER, modelType.getName()));
        }

        this.allProperties = TypeHelper.getProperties(modelType);

        Optional<PropertyDescriptor> idDescriptor = this.allProperties.stream().filter(p -> p.getName().endsWith("Id")).findFirst();

        idProperty = idDescriptor.isPresent() ? idDescriptor.get() : null;
    }

    protected void addEditActions() {
        if (editableCollection != null) {
            if (editableCollection.isDeleteOnly()) {
                addRecordAction("RemoveRecord", Constants.ModuleType.MAIN, TextIds.REMOVE_RECORD, null);
            }
            else
            {
                addCreateAction("AddRecord", Constants.ModuleType.MAIN, TextIds.ADD_RECORD, null);
                addRecordAction("ModifyRecord", Constants.ModuleType.MAIN, TextIds.MODIFY_RECORD, null);
                addRecordAction("RemoveRecord", Constants.ModuleType.MAIN, TextIds.REMOVE_RECORD, null);
            }
        }
    }

    @Override
    protected Object initialize() {
        initializeCollections();

        addEditActions();

        RecordModelCache modelCache = dataCache.get(RecordModelCache.class);

        for (Method method : modelType.getMethods()) {
            if ((method.getModifiers() & Modifier.STATIC) != 0) {
                continue;
            }


            RecordAction actionAttribute = method.getAnnotation(RecordAction.class);
            if (actionAttribute == null) {
                continue;
            }

            if (actionAttribute.minimumAccess() != NodeAccessType.NO_ACCESS) {
                if (getAccessType().getLevel() < actionAttribute.minimumAccess().getLevel()) {
                    continue;
                }
            }

            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length < 1 || !parameters[0].equals(long.class)) {
                continue;
            }

            Action action = new Action();
            action.setText(LanguageHelper.getText(recordModel.getModuleType(), actionAttribute.nameTextId()));
            action.setVisible(actionAttribute.visible());
            action.setType(actionAttribute.createType().equals(Object.class) ? ActionType.RECORD_CREATE : ActionType.RECORD);

            if (!actionAttribute.createType().equals(Object.class)) {
                action.setType(actionAttribute.initialize() ? ActionType.RECORD_INITIALIZE_CREATE : ActionType.RECORD_CREATE);
                action.setParameter(modelCache.getDefinition(actionAttribute.createType()).getId());
            }

            actions.add(action);
        }

        return null;
    }

    protected EditableModelCollection<T> getEditableCollection() {
        return editableCollection;
    }

    protected ModelCollection<T> getCollection() {
        initializeCollections();
        return collection;
    }

    private void initializeCollections() {
        if (collection != null) {
            return;
        }

        collection = createCollection();
        editableCollection = (EditableModelCollection<T>) collection;
        collection.initialize(getObjectId(), getAccessType());

        if (collection == null) {
            throw new IllegalArgumentException(LanguageHelper.getText(Constants.ModuleType.MAIN, TextIds.MISSING_PARAMETER));
        }
    }

    protected ModelCollection<T> createCollection() {
        return (ModelCollection<T>) DependencyResolver.resolve(recordModel.getCollectionType());
    }

    @Override
    public String getViewType() {
        return "RecordList";
    }

    protected boolean isSupportsLiveUpdates() {
        return true;
    }

    protected boolean isSupportsManualRefresh() {
        return false;
    }

    private static String getColumnWidth(RecordColumnDefinition column) {
        if (!StringHelper.isNullOrEmpty(column.getWidth())) {
            return column.getWidth();
        }
        return "*";
    }

    protected void addRecordLink(String text, String url, boolean external) {
        Link link = new Link();
        link.setText(LanguageHelper.getText(recordModel.getModuleType(), text));
        link.setUrl(url);
        link.setExternal(external);
        links.add(link);
    }

    protected void addRecordAction(String name, String moduleType, String text, String visible) {
        Action action = new Action();
        action.setName(name);
        action.setText(LanguageHelper.getText(moduleType, text));
        action.setVisible(visible);
        action.setType(ActionType.RECORD);
        actions.add(action);
    }

    protected void addCreateAction(String name, String moduleType, String text, Class<T> type)
    {
        String typeId = dataCache.get(RecordModelCache.class).getDefinition(type).getId();
        Action action = new Action();
        action.setName(name);
        action.setText(LanguageHelper.getText(moduleType, text));
        action.setType(ActionType.CREATE);
        action.setParameter(typeId);
        actions.add(action);
    }

    protected void addRecordColumnLink(String field, String url)
    {
        columnUrls.put(field, url);
    }

    protected String getColumnLink(String field)
    {
        return columnUrls.get(field);
    }

    @Override
    public JsonObject getViewData() {
        SiteConfiguration siteConfiguration = dataCache.get(SiteConfiguration.class);
        int recordsPerPage = siteConfiguration.getRecordsPerPage();
        ResourceCache resourceCache = dataCache.get(ResourceCache.class, dataCache.get(LanguageCache.class).getCurrentLanguageId());

        try (DomainModel domainModel = domainModelProvider.create()) {
            JsonObject data = new JsonObject();
            data.addProperty("recordsPerPage", recordsPerPage);
            data.addProperty("type", recordModel.getId());
            data.addProperty("extensionScript", getExtensionScript());
            data.addProperty("recordId", StringHelper.toJsonCase(idProperty.getName()));
            data.addProperty("liveUpdates", isSupportsLiveUpdates());
            data.addProperty("manualRefresh", isSupportsManualRefresh());
            data.addProperty("getHasSearch", allProperties.stream().anyMatch(p -> p.getType().equals(String.class) && p.getAnnotation(RecordSearch.class) != null));

            JsonArray actions = new JsonArray();
            this.actions.forEach(a -> {
                JsonObject o = new JsonObject();
                o.addProperty("name", a.getName());
                o.addProperty("text", a.getText());
                o.addProperty("visible", a.getVisible());
                o.addProperty("type", a.getType().toString());
                o.addProperty("parameter", a.getParameter());
                actions.add(o);
            });

            data.add("actions", actions);

            JsonArray links = new JsonArray();

            this.links.forEach(link -> {
                JsonObject o = new JsonObject();
                o.addProperty("text", link.getText());
                o.addProperty("url", link.getUrl());
                o.addProperty("external", link.isExternal());
                links.add(o);
            });

            data.add("links", links);

            data.addProperty("optionsTemplate", resourceCache.readText("Views/RecordOptions.html"));

            JsonArray columns = new JsonArray();

            recordModel.getColumns().forEach(c -> {
                JsonObject o = new JsonObject();

                o.addProperty("width", getColumnWidth(c));
                o.addProperty("name", StringHelper.toJsonCase(c.getProperty().getName()));
                o.addProperty("text", LanguageHelper.getText(recordModel.getModuleType(), c.getTextId()));
                String link = getColumnLink(c.getProperty().getName());
                o.addProperty("url", link != null ? link : c.getCellUrl());
                o.addProperty("sortable", c.isSortable());
                o.addProperty("template", c.getCellTemplate());
                o.addProperty("cellClick", c.getCellUrl());
                o.addProperty("allowUnsafe", c.isAllowUnsafe());
                o.addProperty("cellClass", c.getCellClass());
                o.addProperty("headerClass", c.getHeaderClass());
                o.addProperty("scope", c.getScope());
                o.addProperty("align", c.getAlign().toString());
                o.addProperty("ignoreOptions", c.isIgnoreOptions());
                columns.add(o);
            });

            data.add("columns", columns);

            readRecordsAndIds(data, domainModel, recordsPerPage);

            return data;
        }
    }

    protected int getIndex(T modifiedObject, String filter, DomainModel domainModel) {
        QueryBuilder query = domainModel.query();
        if (recordModel != null && recordModel.getDataType() != null) {
            query = applyFilterAndOrderBy(domainModel.query(), filter);
        }
        List<Long> recordIds = getCollection().readIds(query, false);
        long recordId = (long) idProperty.getValue(modifiedObject);
        return recordIds.indexOf(recordId);
    }

    protected int getIndex(T modifiedObject, String filter) {
        try (DomainModel domainModel = domainModelProvider.create()) {
            return getIndex(modifiedObject, filter, domainModel);
        }
    }

    public JsonObject ModifyRecord(T modifiedObject, String filter, String rawModifiedObject) {
        recordModel.validateInput(JsonSerializer.serializeToTree(modifiedObject), false, rawModifiedObject);

        getEditableCollection().updateRecord(modifiedObject);

        return returnRecordResult(modifiedObject, false, getIndex(modifiedObject, filter));
    }

    public JsonObject createRecord() {
        EditableModelCollection<T> collection = getEditableCollection();

        T record = collection != null ? collection.createRecord() : DependencyResolver.resolve(modelType);

        JsonObject ret = new JsonObject();
        ret.add("record", JsonSerializer.serializeToTree(record));
        return ret;
    }

    public JsonObject addRecord(T newObject, String filter, JsonObject settings, String rawNewObject) {
        if (newObject == null) {
            return returnRecordResult(DependencyResolver.resolve(modelType), false, -1);
        }

        recordModel.validateInput(JsonSerializer.serializeToTree(newObject), true, rawNewObject);

        getEditableCollection().addRecord(newObject);

        return returnRecordResult(newObject, true, getIndex(newObject, filter));
    }

    public JsonObject removeRecord(long recordId, String filter) {
        List<Long> recordIds = new ArrayList<>();
        recordIds.add(recordId);
        getEditableCollection().deleteRecords(recordIds);

        return returnRemovedResult();
    }

    public JsonObject getRecordIds(String filter) {
        ModelCollection<T> collection = getCollection();
        JsonArray recordIds = new JsonArray();

        try (DomainModel domainModel = domainModelProvider.create()) {
            QueryBuilder query = domainModel.query();
            if (recordModel != null && recordModel.getDataType() != null) {
                query = applyFilterAndOrderBy(domainModel.query(), filter);
            }
            collection.readIds(query, false).forEach(recordId -> recordIds.add(new JsonPrimitive(recordId)));
        }

        JsonObject ret = new JsonObject();
        ret.add("recordIds", recordIds);

        return ret;
    }

    @Override
    public boolean processUpdates(OptionalLong fromVersion, long toVersion, JsonObject returnValues, DomainModel domainModel, JsonObject arguments) {
        ModelCollection<T> collection = getCollection();
        ChangesCache changesCache = dataCache.get(ChangesCache.class);
        List<DataChange> changes = changesCache.getCollectionChanges(collection.getCollectionId(), fromVersion, OptionalLong.of(toVersion), null);

        if (changes == null) {
            return false;
        }

        JsonArray removed = new JsonArray();
        JsonArray updated = new JsonArray();

        String filter = null;
        JsonElement filterObj = arguments.get("filter");
        if (filterObj != null) {
            filter = filterObj.getAsString();
        }

        List<Long> recordIds = null;

        for (DataChange change : changes) {
            if (recordIds == null)
            {
                QueryBuilder query = domainModel.query();
                if (recordModel != null && recordModel.getDataType() != null)
                {
                    query = applyFilterAndOrderBy(query, filter);
                }
                recordIds = collection.readIds(query, false);
            }

            if (!recordIds.contains(change.getEntityId())) {
                if (removed.size() == 0) {
                    returnValues.add("records_removed", removed);
                }

                removed.add(new JsonPrimitive(change.getEntityId()));
            }
            else {
                if (updated.size() == 0) {
                    returnValues.add("records_updated", updated);
                }

                updated.add(new JsonPrimitive(change.getEntityId()));
            }
        }

        return removed.size() > 0 || updated.size() > 0;
    }

    private void readRecordsAndIds(JsonObject values, DomainModel domainModel, int recordsPerPage) {
        ModelCollection<T> collection = getCollection();

        List<Long> ids = collection.readIds(domainModel.query(), false);

        JsonArray recordIds = new JsonArray();
        ids.forEach(recordId -> recordIds.add(new JsonPrimitive(recordId)));
        values.add("recordIds", recordIds);

        if (recordsPerPage > 0) {
            List<Long> readRecordIds = new ArrayList<>();
            ids.stream().limit(recordsPerPage).forEach(readRecordIds::add);
            readRecords(values, readRecordIds, domainModel);
        }
    }

    protected void readRecords(JsonObject values, List<Long> ids, DomainModel domainModel) {
        JsonArray array = new JsonArray();
        if (!ids.isEmpty()) {
            Collection<T> records;
            records = getCollection().read(domainModel.query(), ids);
            postProcessRecords(records);
            records.forEach(r -> array.add(JsonSerializer.serializeToTree(r)));
        }
        values.add("records", array);
    }

    protected void postProcessRecords(Collection<T> records) {
    }

    protected JsonObject returnRemovedResult() {
        JsonObject ret = new JsonObject();
        ret.addProperty("removed", true);
        return ret;
    }

    protected JsonObject returnRecordResult(long recordId, boolean isNew, int index) {
        try (DomainModel domainModel = domainModelProvider.create()) {
            List<Long> recordIds = new ArrayList<>();
            recordIds.add(recordId);
            T record = getCollection().read(domainModel.query(), recordIds).iterator().next();
            return returnRecordResult(record, isNew, index);
        }
    }

    protected JsonObject returnRecordResult(T record) {
        return returnRecordResult(record, false);
    }

    protected JsonObject returnRecordResult(T record, boolean isNew) {
        return returnRecordResult(record, isNew, -1);
    }

    protected JsonObject returnRecordResult(T record, boolean isNew, int index) {
        List<T> records = new ArrayList<>();
        records.add(record);
        postProcessRecords(records);

        JsonObject ret = new JsonObject();
        ret.add("record", JsonSerializer.serializeToTree(record));
        ret.addProperty("isNew", isNew);
        ret.addProperty("index", index);
        return ret;
    }

    protected <TN> JsonObject returnNewObjectResult(TN record) {
        JsonObject ret = new JsonObject();
        ret.add("record", JsonSerializer.serializeToTree(record));
        return ret;
    }

    protected JsonObject returnMessageResult(String message) {
        JsonObject ret = new JsonObject();
        ret.addProperty("message", message);
        return ret;
    }

    protected JsonObject returnResetResult() {
        JsonObject ret = new JsonObject();
        ret.addProperty("reset", true);
        return ret;
    }

    protected JsonObject returnRedirectResult(String url) {
        JsonObject ret = new JsonObject();
        ret.addProperty("redirect", url);
        return ret;
    }

    public JsonObject fetch(List<Long> ids) {
        try (DomainModel domainModel = domainModelProvider.create()) {
            JsonObject ret = new JsonObject();
            readRecords(ret, ids, domainModel);
            return ret;
        }
    }

    protected String getExtensionScript() {
        return null;
    }

    public QueryBuilder applyFilterAndOrderBy(QueryBuilder queryBuilder, String filter) {
        if (StringHelper.isNullOrEmpty(filter)) {
            return queryBuilder;
        }

        JsonObject searchProperties = JsonSerializer.deserialize(filter);

        JsonElement searchObject = searchProperties.get("search");
        if (searchObject != null)
        {
            List<Object> filterParams = new ArrayList<>();

            String searchText = "%" + searchObject.getAsString() + "%";

            String filterText = "";

            for (PropertyDescriptor property : allProperties) {
                if (!property.getType().equals(String.class)) {
                    continue;
                }

                if (!dataCache.get(UserCache.class).isAdministrator() && property.getAnnotation(SupportsTextSearch.class) == null) {
                    continue;
                }

                if (filterText.length() > 0) {
                    filterText += " OR ";
                }

                filterText += String.format("%s LIKE ($)", property.getName());
                filterParams.add(searchText);
            }

            if (filterText.length() > 0) {
                queryBuilder = queryBuilder.where("(" + filterText + ")", filterParams);
            }
        }

        JsonElement orderByObject = searchProperties.get("orderBy");
        JsonElement directionObject = searchProperties.get("direction");
        if (orderByObject != null && directionObject != null) {
            String orderBy = orderByObject.getAsString();

            Optional<PropertyDescriptor> parameter = allProperties.stream().filter(p -> p.getName().equals(orderBy)).findFirst();

            if (parameter.isPresent()) {
                boolean ascending = directionObject.getAsString().equals("asc");

                if (ascending) {
                    queryBuilder = queryBuilder.orderBy(String.format("%s", parameter.get().getName()));
                }
                else {
                    queryBuilder = queryBuilder.orderBy(String.format("%s", parameter.get().getName()));
                }
            }
        }

        return queryBuilder;
    }
}
