/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.framework.helpers;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggingHelper {
    public static Logger getLogger(Class<?> type) {
        return Logger.getLogger(type.getName());
    }
    public static void logException(Class<?> type, Throwable e) {
        getLogger(type).log(Level.SEVERE, "Exception occurred", e);
    }
}
