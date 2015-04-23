/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.framework.helpers;

import javax.servlet.http.HttpServletRequest;

public final class UrlHelper {
    public static String getBaseUrl(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.getScheme()).append("://").append(request.getServerName());
        int serverPort = request.getServerPort();
        if (serverPort != 80 && serverPort != 443) {
            builder.append(':').append(serverPort);
        }
        builder.append(request.getContextPath()).append(request.getServletPath());
        return builder.toString();
    }
}
