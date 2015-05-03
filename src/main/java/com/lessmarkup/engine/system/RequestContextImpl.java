/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.system;

import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.UrlHelper;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.system.EngineConfiguration;
import com.lessmarkup.interfaces.system.RequestContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestContextImpl implements RequestContext {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ServletConfig servletConfig;
    private final static String COOKIE_LANGUAGE = "lang";
    private final Map<String, Cookie> responseCookies = new HashMap<>();
    
    private CurrentUser currentUser;
    
    public RequestContextImpl(HttpServletRequest request, HttpServletResponse response, ServletConfig servletConfig) {
        this.request = request;
        this.response = response;
        this.servletConfig = servletConfig;
    }
    
    @Override
    public String getLanguageId() {
        Cookie languageCookie = getCookie(COOKIE_LANGUAGE);
        if (languageCookie == null) {
            return null;
        }

        return languageCookie.getValue();
    }

    @Override
    public void setLanguageId(long languageId) {
        Cookie cookie = new Cookie(COOKIE_LANGUAGE, ((Long)languageId).toString());
        response.addCookie(cookie);
    }

    @Override
    public String getBasePath() {
        return UrlHelper.getBaseUrl(request);
    }

    @Override
    public String getPath() {
        return request.getPathInfo();
    }

    @Override
    public EngineConfiguration getEngineConfiguration() {
        return new EngineConfigurationImpl(servletConfig);
    }

    @Override
    public void redirect(String path) throws IOException {
        response.sendRedirect(getBasePath() + path);
    }

    @Override
    public void sendError(int errorCode) throws IOException {
        response.sendError(errorCode);
    }

    @Override
    public boolean isJsonRequest() {
        return "POST".equals(request.getMethod()) && request.getContentType().startsWith("application/json");
    }

    @Override
    public CurrentUser getCurrentUser() {
        if (currentUser == null) {
            currentUser = DependencyResolver.resolve(CurrentUser.class);
        }
        return currentUser;
    }

    @Override
    public String getRemoteAddress() {
        return request.getRemoteAddr();
    }

    @Override
    public Cookie getCookie(String name) {

        Cookie responseCookie = responseCookies.get(name);
        if (responseCookie != null) {
            return responseCookie;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie: cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        
        return null;
    }

    @Override
    public void setCookie(Cookie cookie) {
        response.addCookie(cookie);
        responseCookies.put(cookie.getName(), cookie);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public void setContentType(String contentType) {
        response.setContentType(contentType);
    }

    @Override
    public void dispatch(String path, Object model) throws ServletException, IOException {
        request.setAttribute("model", model);
        request.getRequestDispatcher(path).forward(request, response);
    }

    @Override
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    @Override
    public String mapPath(String relativePath) {
        return request.getContextPath() + "/" + relativePath;
    }

    @Override
    public String getRootPath() {
        return request.getContextPath();
    }
}
