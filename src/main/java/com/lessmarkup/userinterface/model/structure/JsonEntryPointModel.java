package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.ChangesCache;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.system.RequestContext;
import com.lessmarkup.userinterface.model.common.SearchTextModel;
import com.lessmarkup.userinterface.model.recordmodel.InputFormDefinitionModel;
import com.lessmarkup.userinterface.model.user.LoginModel;
import com.lessmarkup.userinterface.model.user.RegisterModel;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

public class JsonEntryPointModel {
    
    private final DataCache dataCache;
    private final ChangeTracker changeTracker;
    private OptionalLong nodeId = OptionalLong.empty();

    @Inject
    public JsonEntryPointModel(DataCache dataCache, ChangeTracker changeTracker) {
        this.dataCache = dataCache;
        this.changeTracker = changeTracker;
    }
    
    public static boolean appliesToRequest() {
        return RequestContextHolder.getContext().isJsonRequest();
    }
    
    private JsonObject getRequestData() throws IOException {
        try (Reader reader = new InputStreamReader(RequestContextHolder.getContext().getInputStream())) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(reader);
            if (!element.isJsonObject()) {
                return null;
            }
            return element.getAsJsonObject();
        }
    }
    
    private boolean isUserVerified() {

        CurrentUser currentUser = RequestContextHolder.getContext().getCurrentUser();

        return currentUser.isAdministrator() || (currentUser.emailConfirmed() && currentUser.isApproved());
    }
    
    private JsonElement handleDataRequest(JsonObject data, String command, String path) {
        switch (StringHelper.toJsonCase(command)) {
            case "form": {
                InputFormDefinitionModel model = DependencyResolver.resolve(InputFormDefinitionModel.class);
                model.initialize(data.get("id").getAsString());
                return model.toJson();
            }
            case "view": {
                List<String> cachedTemplates = new ArrayList<>();
                data.getAsJsonArray("cached").forEach(e -> cachedTemplates.add(e.getAsString()));
                LoadNodeViewModel model = DependencyResolver.resolve(LoadNodeViewModel.class);
                model.initialize(data.getAsJsonPrimitive("newPath").getAsString(), cachedTemplates, true, false);
                if (model.getNodeId().isPresent()) {
                    nodeId = model.getNodeId();
                }
                return model.toJson();
            }
            case "loginStage1": {
                LoginModel model = DependencyResolver.resolve(LoginModel.class);
                return model.handleStage1Request(data);
            }
            case "loginStage2": {
                LoginModel model = DependencyResolver.resolve(LoginModel.class);
                return model.handleStage2Request(data);
            }
            case "idle": {
                return new JsonPrimitive("");
            }
            case "logout": {
                LoginModel model = DependencyResolver.resolve(LoginModel.class);
                return model.handleLogout();
            }
            case "typeahead": {
                TypeaheadModel model = DependencyResolver.resolve(TypeaheadModel.class);
                model.initialize(path, data.get("property").getAsString(), data.get("searchText").getAsString());
                return model.toJson();
            }
            case "register": {
                RegisterModel model = DependencyResolver.resolve(RegisterModel.class);
                return model.getRegisterObject();
            }
            case "searchText": {
                SearchTextModel model = DependencyResolver.resolve(SearchTextModel.class);
                return model.handle(data.get("text").getAsString());
            }
            default: {
                ExecuteActionModel model = DependencyResolver.resolve(ExecuteActionModel.class);
                return model.handleRequest(data, path);
            }
        }
    }
    
    private void handleUpdates(OptionalLong versionId, String path, JsonObject request, boolean userChanged, JsonObject response) {
        if (userChanged) {
            this.changeTracker.invalidate();
        }
        ChangesCache changesCache = this.dataCache.get(ChangesCache.class);
        scala.Option<Object> newVersionId = changesCache.getLastChangeId();
        OptionalLong newVersionIdJava = newVersionId.isDefined() ? OptionalLong.of((Long) newVersionId.get()) : OptionalLong.empty();
        
        if (newVersionIdJava.isPresent() && !newVersionIdJava.equals(versionId)) {
            response.add("versionId", new JsonPrimitive(newVersionIdJava.getAsLong()));
        }
        
        if (userChanged) {
            UserInterfaceElementsModel notificationsModel = DependencyResolver.resolve(UserInterfaceElementsModel.class);
            notificationsModel.handle(response, newVersionIdJava);
        }
        
        if (newVersionId.get().equals(versionId) && !this.nodeId.isPresent()) {
            return;
        }
        
        LoadUpdatesModel model = DependencyResolver.resolve(LoadUpdatesModel.class);
        model.handle(versionId, newVersionIdJava, path, request, response, this.nodeId);
    }
    
    public void handleRequest() throws IOException {
        
        RequestContext requestContext = RequestContextHolder.getContext();
        
        JsonObject requestData = getRequestData();
        JsonElement path = requestData.get("path");
        if (path == null) {
            requestContext.sendError(404);
            return;
        }
        JsonElement command = requestData.get("command");
        if (command == null) {
            requestContext.sendError(404);
            return;
        }
        JsonObject response = new JsonObject();

        CurrentUser currentUser = RequestContextHolder.getContext().getCurrentUser();

        OptionalLong userId = currentUser.getUserIdJava();
        boolean userVerified = isUserVerified();
        boolean administrator = currentUser.isAdministrator();
        
        try {
            JsonElement resultData = handleDataRequest(requestData, command.getAsString(), path.getAsString());
            if (resultData.isJsonObject()) {
                JsonObject resultObject = resultData.getAsJsonObject();
                if (resultObject.has("versionId")) {
                    JsonElement versionId = resultObject.get("versionId");
                    handleUpdates(versionId != null ? OptionalLong.of(versionId.getAsLong()) : OptionalLong.empty(), path.getAsString(), requestData, userId != currentUser.getUserIdJava(), response);
                }
            }
            response.add("data", resultData);
            response.addProperty("success", true);
        } catch (Exception e) {
            LoggingHelper.logException(getClass(), e);
            response.addProperty("success", false);
            String message = e.getMessage();
            response.addProperty("message", StringHelper.getMessage(e));
        }

        JsonObject userState = new JsonObject();
        response.add("user", userState);

        userId = currentUser.getUserIdJava();
        userState.addProperty("loggedIn", userId.isPresent());

        if (userId.isPresent()) {
            userState.addProperty("userName", currentUser.getUserName().get());
        }

        if (userVerified != isUserVerified()) {
            userState.addProperty("userNotVerified", userVerified);
        }
        if (administrator != currentUser.isAdministrator()) {
            userState.addProperty("showConfiguration", currentUser.isAdministrator());
        }
        
        try (OutputStream output = requestContext.getOutputStream()) {
            output.write(response.toString().getBytes());
        }
        requestContext.setContentType("application/json");
    }
}
