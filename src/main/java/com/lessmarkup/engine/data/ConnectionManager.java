/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {
    public static Connection getConnection(String connectionString) throws SQLException, ClassNotFoundException {
        
        int driverPart = connectionString.indexOf(';');
        if (driverPart > 0 && driverPart < connectionString.indexOf(':')) {
            String driverClass = connectionString.substring(0, driverPart);
            connectionString = connectionString.substring(driverPart+1);
            Class.forName(driverClass);
        }
        
        return DriverManager.getConnection(connectionString);
    }
}
