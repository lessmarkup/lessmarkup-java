package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.lessmarkup.Constants;
import com.lessmarkup.engine.filesystem.TemplateContext;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.ImageHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.ChangesCache;
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
import java.util.OptionalLong;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

@Component
@Scope("prototype")
public class NodeEntryPointModel {

    public static class Context extends TemplateContext {
        private boolean noScript = false;
        private boolean onlyBody = false;
        private String title;
        private final DataCache dataCache;
        private String initialData;
        private String configurationData;
        
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

        public String getConfigurationData() { return configurationData; }
        
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
            String analyticsObject =
                    "<script>(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){" +
                            "(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o)," +
                            "m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)" +
                            "})(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');";
            analyticsObject += String.format("ga('create', '%s', 'auto');</script>", resourceId);
            return analyticsObject;
        }
    }

    private final Context context;
    private LoadNodeViewModel viewData;
    private final JsonObject initialData = new JsonObject();
    private final JsonObject configurationData = new JsonObject();
    private final DataCache dataCache;
    private final CurrentUser currentUser;
    
    @Autowired
    public NodeEntryPointModel(DataCache dataCache, CurrentUser currentUser) {
        this.dataCache = dataCache;
        this.currentUser = currentUser;
        this.context = new Context(dataCache);
    }

    public String getResource(String path) {
        return dataCache.get(ResourceCache.class).readText(path);
    }

    public String getInitialData() {
        return initialData.toString();
    }

    public String getConfigurationData() { return configurationData.toString(); }

    private void checkBrowser(RequestContext requestContext) {
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
            context.onlyBody = true;
        }
        
        String nodeLoadError = null;
        
        viewData = DependencyResolver.resolve(LoadNodeViewModel.class);
        try {
            if (!viewData.initialize(path, null, true, true)) {
                return false;
            }
        } catch (Exception e) {
            nodeLoadError = e.getMessage();
            if (nodeLoadError == null) {
                nodeLoadError = e.toString();
            }
        }
        
        NodeCache nodeCache = this.dataCache.get(NodeCache.class);
        RecordModelCache recordModelCache = this.dataCache.get(RecordModelCache.class);
        
        CachedNodeInformation rootNode = nodeCache.getRootNode();
        
        context.title = rootNode.getTitle();
        
        initializeSiteProperties(requestContext, initialData);
        
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
        
        initialData.addProperty("rootPath", requestContext.getRootPath());
        initialData.addProperty("path", path != null ? path : "");
        initialData.addProperty("showConfiguration", this.currentUser.isAdministrator());
        initialData.addProperty("configurationPath", "/" + Constants.NodePath.CONFIGURATION);
        initialData.addProperty("profilePath", "/" + Constants.NodePath.PROFILE);
        initialData.addProperty("forgotPasswordPath", "/" + Constants.NodePath.FORGOT_PASSWORD);
        initialData.addProperty("loggedIn", this.currentUser.getUserId().isPresent());
        initialData.addProperty("userNotVerified", !this.currentUser.isApproved() || !this.currentUser.emailConfirmed());
        if (this.currentUser.getUserName() != null) {
            initialData.addProperty("userName", this.currentUser.getUserName());
        }
        initialData.add("viewData", this.viewData.toJson());
        if (nodeLoadError != null) {
            initialData.addProperty("nodeLoadError", nodeLoadError);
        }
        initialData.addProperty("recaptchaPublicKey", requestContext.getEngineConfiguration().getRecaptchaPublicKey());
        initialData.addProperty("loginModelId", recordModelCache.getDefinition(LoginModel.class).getId());

        context.initialData = getInitialData();
        context.configurationData = getConfigurationData();

        return true;
    }
    
    private void initializeSiteProperties(RequestContext request) {
        ChangesCache changesCache = this.dataCache.get(ChangesCache.class);
        OptionalLong versionId = changesCache.getLastChangeId();
        if (versionId.isPresent()) {
            initialValues.addProperty("versionId", versionId.getAsLong());
        } else {
            initialValues.add("versionId", JsonNull.INSTANCE);
        }
        SiteConfiguration siteConfiguration = this.dataCache.get(SiteConfiguration.class);
        String adminLoginPage = siteConfiguration.getAdminLoginPage();
        if (adminLoginPage == null || adminLoginPage.length() == 0) {
            adminLoginPage = RequestContextHolder.getContext().getEngineConfiguration().getAdminLoginPage();
        }
        initialValues.addProperty("hasLogin", siteConfiguration.getHasUsers() || adminLoginPage == null || adminLoginPage.length() == 0);
        initialValues.addProperty("getHasSearch", siteConfiguration.getHasSearch());
        
        UserInterfaceElementsModel notificationsModel = DependencyResolver.resolve(UserInterfaceElementsModel.class);
        notificationsModel.handle(initialValues, versionId);
        
        if (siteConfiguration.getHasLanguages()) {
            LanguageCache languageCache = this.dataCache.get(LanguageCache.class);
            OptionalLong languageId = languageCache.getCurrentLanguageId();

            JsonArray languages = new JsonArray();
            
            for (Language language: languageCache.getLanguages()) {
                JsonObject data = new JsonObject();
                data.addProperty("id", language.getLanguageId());
                data.addProperty("name", language.getName());
                data.addProperty("shortName", language.getShortName());
                data.addProperty("url", "/language/" + Long.toString(language.getLanguageId()));
                if (language.getIconId().isPresent()) {
                    data.addProperty("imageUrl", ImageHelper.getImageUrl(language.getIconId().getAsLong()));
                }
                data.addProperty("selected", languageId.isPresent() && languageId.getAsLong() == language.getLanguageId());
                languages.add(data);
            }
            
            initialValues.add("languages", languages);
        }
        
        initialValues.addProperty("rootTitle", siteConfiguration.getSiteName());
        initialValues.addProperty("maximumFileSize", siteConfiguration.getMaximumFileSize());
        initialValues.addProperty("useGoogleAnalytics", siteConfiguration.getGoogleAnalyticsResource() != null);
    }
    
    public void handleRequest() throws ServletException, IOException {
        RequestContext requestContext = RequestContextHolder.getContext();
        ResourceCache resourceCache = dataCache.get(ResourceCache.class, dataCache.get(LanguageCache.class).getCurrentLanguageId());
        Mustache.Compiler compiler = TemplateContext.createCompiler(resourceCache);
        Template template = compiler.compile(resourceCache.readText("views/entrypoint.html"));
        String html = template.execute(context);
        try (Writer writer = new OutputStreamWriter(requestContext.getOutputStream())) {
            writer.write(html);
        }
    }
}
