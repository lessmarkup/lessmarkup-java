package com.lessmarkup.interfaces.exceptions;

import java.sql.SQLException;

public class DatabaseException extends RuntimeException {
    public DatabaseException(SQLException e) {
        super(e.getMessage(), e);
    }
}
