/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.data.dialects;

import java.util.OptionalInt;

public class MicrosoftSqlDialect implements DatabaseLanguageDialect {

    @Override
    public String getTypeDeclaration(DatabaseDataType type, OptionalInt sizeLimit, boolean required, String characterSet) {
        String nullable = required ? " NOT NULL" : " NULL";
        switch (type) {
            case INT:
                return "[INT]" + nullable;
            case LONG:
                return "[BIGINT]" + nullable;
            case DATE_TIME:
                return "[DATETIME]" + nullable;
            case IDENTITY:
                return "[BIGINT] IDENTITY(1,1)" + nullable;
            case BOOLEAN:
                return "[BIT]" + nullable;
            case FLOAT:
                return "[FLOAT]" + nullable;
            case DOUBLE:
                return "[DOUBLE]" + nullable;
            case STRING:
                return String.format("[NVARCHAR](%s)%s", sizeLimit.isPresent() ? Integer.toString(sizeLimit.getAsInt()) : "max", nullable);
            case BINARY:
                return String.format("[VARBINARY](%s)%s", sizeLimit.isPresent() ? Integer.toString(sizeLimit.getAsInt()) : "max", nullable);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String decorateName(String name) {
        return "[" + name + "]";
    }

    @Override
    public DatabaseDataType getDataType(String dataType) {
        switch (dataType.toUpperCase()) {
            case "INT":
                return DatabaseDataType.INT;
            case "BIGINT":
                return DatabaseDataType.LONG;
            case "DATETIME":
                return DatabaseDataType.DATE_TIME;
            case "BIT":
                return DatabaseDataType.BOOLEAN;
            case "FLOAT":
                return DatabaseDataType.FLOAT;
            case "DOUBLE":
                return DatabaseDataType.DOUBLE;
            case "NVARCHAR":
                return DatabaseDataType.STRING;
            case "VARBINARY":
                return DatabaseDataType.BINARY;
            default:
                return null;
        }
    }

    @Override
    public String paging(int from, int count) {
        return String.format("OFFSET %d ROWS FETCH NEXT %d ROWS ONLY", from, count);
    }
}
