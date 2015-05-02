/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.interfaces.system;

import com.lessmarkup.interfaces.security.CurrentUser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.OptionalLong;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

public interface RequestContext {
    String getLanguageId();
    void setLanguageId(long languageId);
    String getBasePath();
    String getPath();
    String getRootPath();
    EngineConfiguration getEngineConfiguration();
    void redirect(String path) throws IOException;
    void sendError(int errorCode) throws IOException;
    boolean isJsonRequest();
    CurrentUser getCurrentUser();
    String getRemoteAddress();
    Cookie getCookie(String name);
    void setCookie(Cookie cookie);
    InputStream getInputStream() throws IOException;
    OutputStream getOutputStream() throws IOException;
    void setContentType(String contentType);
    void dispatch(String path, Object model) throws ServletException, IOException;
    void addHeader(String name, String value);
    String mapPath(String relativePath);
}
