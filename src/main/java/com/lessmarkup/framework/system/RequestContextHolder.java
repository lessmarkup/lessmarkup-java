/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.framework.system;

import com.lessmarkup.interfaces.system.RequestContext;

public final class RequestContextHolder {
    
    private static final ThreadLocal contextHolder = new ThreadLocal();
    
    public static void onRequestStarted(RequestContext context) {
        contextHolder.set(context);
    }
    
    public static void onRequestFinished() {
        contextHolder.remove();
    }
    
    public static RequestContext getContext() {
        return (RequestContext) contextHolder.get();
    }
}
