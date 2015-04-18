package com.lessmarkup;

import com.lessmarkup.engine.data.migrate.MigrateEngine;
import com.lessmarkup.engine.system.RequestContextImpl;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.module.ModuleActionHandler;
import com.lessmarkup.interfaces.module.ModuleIntegration;
import com.lessmarkup.interfaces.system.RequestContext;
import com.lessmarkup.userinterface.model.structure.JsonEntryPointModel;
import com.lessmarkup.userinterface.model.structure.NodeEntryPointModel;
import com.lessmarkup.userinterface.model.structure.ResourceModel;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet extends HttpServlet {

    private static final Pattern languagePattern = Pattern.compile("language/([0-9]+)");
    
    private boolean processRequestInContext(String path, RequestContext requestContext) throws ServletException, IOException {
        LoggingHelper.getLogger(getClass()).info(String.format("Request to '%s'", path));
        
        if (path.startsWith(Constants.NodePath.SYSTEM_ACTION + "/")) {
            
            String actionPath = path.substring(Constants.NodePath.SYSTEM_ACTION.length()+1);
            
            int pos = actionPath.indexOf('/');
            
            String name;
            
            if (pos <= 0) {
                name = actionPath;
                actionPath = null;
            } else {
                name = actionPath.substring(0, pos);
                actionPath = actionPath.substring(pos+1);
            }
            
            ModuleActionHandler action = DependencyResolver.resolve(ModuleIntegration.class).getActionHandler(name);
            
            if (action != null) {
                action.handleAction(actionPath);
                return true;
            }
        }
        
        Matcher match = languagePattern.matcher(path);
        if (match.matches() && match.groupCount() == 1) {
            LoggingHelper.getLogger(getClass()).info("Handling language change request");
            try {
                long languageId = Long.parseLong(match.group(0));
                
                requestContext.setLanguageId(languageId);
                requestContext.redirect("/");
                return true;
            } catch (NumberFormatException e) {
                LoggingHelper.getLogger(getClass()).warning("Unknown language id");
                requestContext.sendError(400);
                return true;
            }
        }
        
        if (JsonEntryPointModel.appliesToRequest()) {
            LoggingHelper.getLogger(getClass()).info("Start of JSON request");
            JsonEntryPointModel model = DependencyResolver.resolve(JsonEntryPointModel.class);
            model.handleRequest();
            LoggingHelper.getLogger(getClass()).info("End of JSON request");
            return true;
        }
        
        NodeEntryPointModel nodeModel = DependencyResolver.resolve(NodeEntryPointModel.class);
        if (nodeModel.initialize(path)) {
            nodeModel.handleRequest();
            return true;
        }
        
        ResourceModel resourceModel = DependencyResolver.resolve(ResourceModel.class);
        if (resourceModel.initialize(path)) {
            LoggingHelper.getLogger(getClass()).info("Handling resource access request");
            resourceModel.handleRequest();
            return true;
        }
        
        return false;
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getPathInfo();
        
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        RequestContextImpl requestContext = new RequestContextImpl(request, response, getServletConfig());

        RequestContextHolder.onRequestStarted(requestContext);
        
        try {
            DependencyResolver.resolve(ChangeTracker.class).enqueueUpdates();
        
            if (!processRequestInContext(path, requestContext)) {
                response.sendError(404);
            }
        } finally {
            RequestContextHolder.onRequestFinished();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "LessMarkup";
    }// </editor-fold>

    @Override
    public void init() throws ServletException {
        super.init();

        LoggingHelper.getLogger(getClass()).info("Initializing application");

        RequestContextImpl requestContext = new RequestContextImpl(null, null, getServletConfig());
        RequestContextHolder.onRequestStarted(requestContext);
        
        try {
            ModuleProvider moduleProvider = DependencyResolver.resolve(ModuleProvider.class);

            moduleProvider.discoverAndRegisterModules();

            DependencyResolver.resolve(DomainModelProvider.class).initialize();

            MigrateEngine migrateEngine = DependencyResolver.resolve(MigrateEngine.class);
            migrateEngine.execute();
            
            moduleProvider.updateModuleDatabase(DependencyResolver.resolve(DomainModelProvider.class));
        } finally {
            RequestContextHolder.onRequestFinished();
        }

        LoggingHelper.getLogger(getClass()).info("Successfully initialized application");
    }
}
