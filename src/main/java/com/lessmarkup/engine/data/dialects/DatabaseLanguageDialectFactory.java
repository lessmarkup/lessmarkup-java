package com.lessmarkup.engine.data.dialects;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseLanguageDialectFactory {
    public static DatabaseLanguageDialect createDialect(Connection connection) throws SQLException {
        switch (connection.getMetaData().getDriverName()) {
            default:
                return new MySqlDialect();
        }
    }
}
