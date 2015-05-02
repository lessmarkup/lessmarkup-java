package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.lessmarkup.Constants;
import com.lessmarkup.dataobjects.Smile;
import com.lessmarkup.engine.filesystem.TemplateContext;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.ImageHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.ChangesCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.recordmodel.RecordModelCache;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.structure.CachedNodeInformation;
import com.lessmarkup.interfaces.structure.NodeCache;
import com.lessmarkup.interfaces.system.*;
import com.lessmarkup.userinterface.model.user.LoginModel;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;
import java.util.OptionalLong;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

@Component
@Scope("prototype")
public class NodeEntryPointModel {

    public static class Context extends TemplateContext {
        private boolean noScript = false;
        private String title;
        private final DataCache dataCache;
        private String initialData;
        private String serverConfiguration;
        private String languages;
        
        public Context(DataCache dataCache) {
            super(dataCache);
            this.dataCache = dataCache;
        }

        public boolean getUseScripts() {
            return !noScript;
        }

        public String getTitle() {
            return title;
        }
        
        public String getInitialData() {
            return initialData;
        }

        public String getServerConfiguration() { return serverConfiguration; }

        public String getLanguages() { return this.languages; }
        
        public String getTopMenu() {
            StringBuilder builder = new StringBuilder();
            NodeCache nodeCache = this.dataCache.get(NodeCache.class);
            nodeCache.getNodes().stream().filter(n -> n.isAddToMenu() && n.isVisible()).forEach(menuNode -> builder.append(String.format(
                "<li ng-style=\"{ active: path === '%s' }}\"><a href=\"%s\" ng-click=\"navigateToView('%s')\">%s</a></li>",
                    menuNode.getFullPath(), menuNode.getFullPath(), menuNode.getFullPath(), menuNode.getTitle())));
            return builder.toString();
        }
        
        public String getNoScriptBlock() {
            return Constants.Engine.NO_SCRIPT_BLOCK;
        }

        public String getGoogleAnalytics() {
            SiteConfiguration configuration = this.dataCache.get(SiteConfiguration.class);
            String resourceId = configuration.getGoogleAnalyticsResource();

            if (StringHelper.isNullOrWhitespace(resourceId)) {
                return "";
            }
            return String.format(this.dataCache.get(ResourceCache.class).readText("/views/googleAnalytics.html"), resourceId);
        }
    }

    private final Context context;
    private LoadNodeViewModel viewData;
    private final DataCache dataCache;
    private final CurrentUser currentUser;
    private final DomainModelProvider domainModelProvider;

    private String nodeLoadError;
    private OptionalLong versionId;
    
    @Autowired
    public NodeEntryPointModel(DataCache dataCache, CurrentUser currentUser, DomainModelProvider domainModelProvider) {
        this.dataCache = dataCache;
        this.currentUser = currentUser;
        this.context = new Context(dataCache);
        this.domainModelProvider = domainModelProvider;
    }

    private void checkBrowser(RequestContext requestContext) {
    }
    
    private void prepareInitialData(RequestContext requestContext, String path) {
        JsonObject initialData = new JsonObject();

        ChangesCache changesCache = this.dataCache.get(ChangesCache.class);
        this.versionId = changesCache.getLastChangeId();

        initialData.addProperty("loggedIn", this.currentUser.getUserId().isPresent());
        initialData.addProperty("userNotVerified", !this.currentUser.isApproved() || !this.currentUser.emailConfirmed());
        if (this.currentUser.getUserName() != null) {
            initialData.addProperty("userName", this.currentUser.getUserName());
        } else {
            initialData.add("userName", JsonNull.INSTANCE);
        }
        initialData.addProperty("showConfiguration", this.currentUser.isAdministrator());

        if (versionId.isPresent()) {
            initialData.addProperty("versionId", versionId.getAsLong());
        } else {
            initialData.add("versionId", JsonNull.INSTANCE);
        }

        initialData.add("loadedNode", this.viewData.toJson());

        initialData.addProperty("path", path != null ? path : "");


        if (nodeLoadError != null) {
            initialData.addProperty("nodeLoadError", nodeLoadError);
        }

        this.context.initialData = initialData.toString();
    }

    private void prepareServerConfiguration(RequestContext requestContext) {
        JsonObject serverConfiguration = new JsonObject();

        EngineConfiguration engineConfiguration = requestContext.getEngineConfiguration();
        SiteConfiguration siteConfiguration = this.dataCache.get(SiteConfiguration.class);

        String adminLoginPage = siteConfiguration.getAdminLoginPage();
        if (adminLoginPage == null || adminLoginPage.length() == 0) {
            adminLoginPage = RequestContextHolder.getContext().getEngineConfiguration().getAdminLoginPage();
        }
        serverConfiguration.addProperty("hasLogin", siteConfiguration.getHasUsers() || adminLoginPage == null || adminLoginPage.length() == 0);
        serverConfiguration.addProperty("hasSearch", siteConfiguration.getHasSearch());
        serverConfiguration.addProperty("configurationPath", "/" + Constants.NodePath.CONFIGURATION);
        serverConfiguration.addProperty("rootPath", requestContext.getRootPath());
        serverConfiguration.addProperty("rootTitle", siteConfiguration.getSiteName());
        serverConfiguration.addProperty("profilePath", "/" + Constants.NodePath.PROFILE);
        serverConfiguration.addProperty("forgotPasswordPath", "/" + Constants.NodePath.FORGOT_PASSWORD);

        UserInterfaceElementsModel notificationsModel = DependencyResolver.resolve(UserInterfaceElementsModel.class);
        notificationsModel.handle(serverConfiguration, this.versionId);

        serverConfiguration.addProperty("recaptchaPublicKey", engineConfiguration.getRecaptchaPublicKey());
        serverConfiguration.addProperty("maximumFileSize", siteConfiguration.getMaximumFileSize());

        try (DomainModel domainModel = this.domainModelProvider.create()) {

            JsonArray smiles = new JsonArray();
            serverConfiguration.add("smiles", smiles);

            for (Smile smile : domainModel.query().from(Smile.class).toList(Smile.class)) {
                JsonObject smileTarget = new JsonObject();
                smileTarget.addProperty("id", smile.getId());
                smileTarget.addProperty("code", smile.getCode());
                smiles.add(smileTarget);
            }

            serverConfiguration.addProperty("smilesBase", "/image/smile/");
        }

        serverConfiguration.addProperty("useGoogleAnalytics", siteConfiguration.getGoogleAnalyticsResource() != null);

        RecordModelCache recordModelCache = this.dataCache.get(RecordModelCache.class);
        serverConfiguration.addProperty("loginModelId", recordModelCache.getDefinition(LoginModel.class).getId());

        serverConfiguration.addProperty("pageSize", engineConfiguration.getRecordsPerPage());

        this.context.serverConfiguration = serverConfiguration.toString();
    }

    private void prepareLanguages() {
        JsonArray languages = new JsonArray();

        LanguageCache languageCache = this.dataCache.get(LanguageCache.class);

        for (Language sourceLanguage: languageCache.getLanguages()) {
            JsonObject targetLanguage = new JsonObject();
            targetLanguage.addProperty("selected", false);
            targetLanguage.addProperty("id", sourceLanguage.getShortName().toLowerCase());
            targetLanguage.addProperty("shortName", sourceLanguage.getShortName());
            targetLanguage.addProperty("name", sourceLanguage.getName());
            targetLanguage.addProperty("isDefault", sourceLanguage.getIsDefault());
            targetLanguage.addProperty("iconUrl", sourceLanguage.getIconId().isPresent() ? ImageHelper.getImageUrl(sourceLanguage.getIconId().getAsLong()) : "");

            JsonObject translations = new JsonObject();

            for (Map.Entry<String, String> translation : sourceLanguage.getTranslations().entrySet()) {
                translations.addProperty(translation.getKey(), translation.getValue());
            }

            targetLanguage.add("translations", translations);

            languages.add(targetLanguage);
        }

        this.context.languages = languages.toString();
    }
    
    public boolean initialize(String path) {

        RequestContext requestContext = RequestContextHolder.getContext();

        checkBrowser(requestContext);
        
        if (!context.noScript) {
            Cookie cookie = requestContext.getCookie("noscript");
            if (cookie != null && "true".equals(cookie.getValue())) {
                context.noScript = true;
            }
        }
        
        String queryString = requestContext.getPath();
        
        if (queryString != null && (queryString.endsWith("?noscript") || "noscript".equals(queryString))){
            context.noScript = true;
        }
        
        this.nodeLoadError = null;
        
        viewData = DependencyResolver.resolve(LoadNodeViewModel.class);
        try {
            if (!viewData.initialize(path, null, true, true)) {
                return false;
            }
        } catch (Exception e) {
            this.nodeLoadError = e.getMessage();
            if (this.nodeLoadError == null) {
                this.nodeLoadError = e.toString();
            }
        }
        
        NodeCache nodeCache = this.dataCache.get(NodeCache.class);

        CachedNodeInformation rootNode = nodeCache.getRootNode();
        
        context.title = rootNode.getTitle();
        
        prepareInitialData(requestContext, path);
        prepareServerConfiguration(requestContext);
        prepareLanguages();
        
        if (context.noScript) {
            /*int pos = context.body.indexOf(Constants.Engine.NO_SCRIPT_BLOCK);
            if (pos <= 0) {
                return true;
            }
            
            String viewPath = LoadNodeViewModel.getViewPath(viewData.getNodeHandler().getViewType());
            ResourceCache resourceCache = this.dataCache.get(ResourceCache.class, this.dataCache.get(LanguageCache.class).getCurrentLanguageId());
            String template = resourceCache.readText(viewPath + ".html");
            context.body = context.onlyBody ? template : context.body.substring(0, pos) + template + context.body.substring(pos+Constants.Engine.NO_SCRIPT_BLOCK.length());*/
            return true;
        }
        
        return true;
    }

    public void handleRequest() throws ServletException, IOException {
        RequestContext requestContext = RequestContextHolder.getContext();
        ResourceCache resourceCache = dataCache.get(ResourceCache.class);
        Mustache.Compiler compiler = TemplateContext.createCompiler(resourceCache);
        Template template = compiler.compile(resourceCache.readText("views/entrypoint.html"));
        String html = template.execute(context);
        try (Writer writer = new OutputStreamWriter(requestContext.getOutputStream())) {
            writer.write(html);
        }
    }
}
