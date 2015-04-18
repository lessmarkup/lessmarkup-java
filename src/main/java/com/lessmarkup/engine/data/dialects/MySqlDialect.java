/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.data.dialects;

import java.util.OptionalInt;

public class MySqlDialect implements DatabaseLanguageDialect {

    @Override
    public String getTypeDeclaration(DatabaseDataType type, OptionalInt sizeLimit, boolean required, String characterSet) {
        String nullable = required ? " NOT NULL" : " NULL";
        switch (type){
            case INT:
                return "INT" + nullable;
            case LONG:
                return "BIGINT" + nullable;
            case DATE_TIME:
                return "DATETIME" + nullable;
            case STRING:
                if (characterSet == null) {
                    characterSet = "utf8";
                }
                if (!sizeLimit.isPresent()) {
                    return "TEXT CHARACTER SET " + characterSet + nullable;
                }
                return String.format("VARCHAR(%s) CHARACTER SET " + characterSet, sizeLimit.getAsInt()) + nullable;
            case BOOLEAN:
                return "BIT" + nullable;
            case FLOAT:
                return "REAL" + nullable;
            case DOUBLE:
                return "DOUBLE" + nullable;
            case BINARY:
                return "BLOB" + nullable;
            case IDENTITY:
                return "BIGINT" + nullable + " AUTO_INCREMENT";
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String decorateName(String name) {
        return "`" + name + "`";
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
            case "TEXT":
            case "VARCHAR":
                return DatabaseDataType.STRING;
            case "BIT":
                return DatabaseDataType.BOOLEAN;
            case "REAL":
                return DatabaseDataType.FLOAT;
            case "DOUBLE":
                return DatabaseDataType.DOUBLE;
            case "BLOB":
                return DatabaseDataType.BINARY;
            default:
                return null;
        }
    }

    @Override
    public String paging(int from, int count) {
        return String.format("LIMIT %d,%d", from, count);
    }
}
