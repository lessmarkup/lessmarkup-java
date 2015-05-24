package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
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

import java.io.*;
import java.util.Map;
import java.util.OptionalLong;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import scala.collection.JavaConversions;

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
            return Constants.EngineNoScriptBlock();
        }

        public String getGoogleAnalytics() {
            SiteConfiguration configuration = this.dataCache.get(SiteConfiguration.class);
            String resourceId = configuration.getGoogleAnalyticsResource();

            if (StringHelper.isNullOrWhitespace(resourceId)) {
                return "";
            }
            return String.format(this.dataCache.get(ResourceCache.class).readTextJava("/views/googleAnalytics.html"), resourceId);
        }
    }

    private final Context context;
    private LoadNodeViewModel viewData;
    private final DataCache dataCache;
    private final DomainModelProvider domainModelProvider;

    private String nodeLoadError;
    private OptionalLong versionId;
    
    @Inject
    public NodeEntryPointModel(DataCache dataCache, DomainModelProvider domainModelProvider) {
        this.dataCache = dataCache;
        this.context = new Context(dataCache);
        this.domainModelProvider = domainModelProvider;
    }

    private void checkBrowser(RequestContext requestContext) {
    }
    
    private void prepareInitialData(RequestContext requestContext, String path) {
        JsonObject initialData = new JsonObject();

        ChangesCache changesCache = this.dataCache.get(ChangesCache.class);

        scala.Option<Object> versionId = changesCache.getLastChangeId();

        this.versionId = versionId.isDefined() ? OptionalLong.of((Long)versionId.get()) : OptionalLong.empty();

        CurrentUser currentUser = RequestContextHolder.getContext().getCurrentUser();

        initialData.addProperty("loggedIn", currentUser.getUserId().isDefined());
        initialData.addProperty("userNotVerified", !currentUser.isApproved() || !currentUser.emailConfirmed());
        if (currentUser.getUserName().isDefined()) {
            initialData.addProperty("userName", currentUser.getUserName().get());
        } else {
            initialData.add("userName", JsonNull.INSTANCE);
        }
        initialData.addProperty("showConfiguration", currentUser.isAdministrator());

        if (this.versionId.isPresent()) {
            initialData.addProperty("versionId", this.versionId.getAsLong());
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
        serverConfiguration.addProperty("configurationPath", "/" + Constants.NodePathConfiguration());
        serverConfiguration.addProperty("rootPath", requestContext.getRootPath());
        serverConfiguration.addProperty("rootTitle", siteConfiguration.getSiteName());
        serverConfiguration.addProperty("profilePath", "/" + Constants.NodePathProfile());
        serverConfiguration.addProperty("forgotPasswordPath", "/" + Constants.NodePathForgotPassword());

        UserInterfaceElementsModel notificationsModel = DependencyResolver.resolve(UserInterfaceElementsModel.class);
        notificationsModel.handle(serverConfiguration, this.versionId);

        serverConfiguration.addProperty("recaptchaPublicKey", engineConfiguration.getRecaptchaPublicKey());
        serverConfiguration.addProperty("maximumFileSize", siteConfiguration.getMaximumFileSize());

        try (DomainModel domainModel = this.domainModelProvider.create()) {

            JsonArray smiles = new JsonArray();
            serverConfiguration.add("smiles", smiles);

            for (Smile smile : domainModel.query().from(Smile.class).toListJava(Smile.class)) {
                JsonObject smileTarget = new JsonObject();
                smileTarget.addProperty("id", smile.getId());
                smileTarget.addProperty("code", smile.getCode());
                smiles.add(smileTarget);
            }

            serverConfiguration.addProperty("smilesBase", "/image/smile/");
        }

        serverConfiguration.addProperty("useGoogleAnalytics", siteConfiguration.getGoogleAnalyticsResource() != null);

        RecordModelCache recordModelCache = this.dataCache.get(RecordModelCache.class);
        serverConfiguration.addProperty("loginModelId", recordModelCache.getDefinition(LoginModel.class).get().getId());

        serverConfiguration.addProperty("pageSize", engineConfiguration.getRecordsPerPage());

        this.context.serverConfiguration = serverConfiguration.toString();
    }

    private void prepareLanguages() {
        JsonArray languages = new JsonArray();

        LanguageCache languageCache = this.dataCache.get(LanguageCache.class);

        for (Language sourceLanguage: JavaConversions.asJavaCollection(languageCache.getLanguages())) {
            JsonObject targetLanguage = new JsonObject();
            targetLanguage.addProperty("selected", false);
            targetLanguage.addProperty("id", sourceLanguage.getShortName().toLowerCase());
            targetLanguage.addProperty("shortName", sourceLanguage.getShortName());
            targetLanguage.addProperty("name", sourceLanguage.getName());
            targetLanguage.addProperty("isDefault", sourceLanguage.getIsDefault());
            targetLanguage.addProperty("iconUrl", sourceLanguage.getIconId().isDefined() ? ImageHelper.getImageUrl((Long)sourceLanguage.getIconId().get()) : "");

            JsonObject translations = new JsonObject();

            for (scala.Tuple2<String, String> translation : JavaConversions.asJavaIterable(sourceLanguage.getTranslations().toIterable())) {
                translations.addProperty(translation._1(), translation._2());
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
        Template template = compiler.compile(resourceCache.readTextJava("views/entrypoint.html"));
        String html = template.execute(context);
        try (Writer writer = new OutputStreamWriter(requestContext.getOutputStream())) {
            writer.write(html);
        }
    }
}
