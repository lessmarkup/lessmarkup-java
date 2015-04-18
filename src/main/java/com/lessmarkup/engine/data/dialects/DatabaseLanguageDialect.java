/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.data.dialects;

import java.util.OptionalInt;

public interface DatabaseLanguageDialect {
    String getTypeDeclaration(DatabaseDataType type, OptionalInt sizeLimit, boolean required, String characterSet);
    default String getTypeDeclaration(DatabaseDataType type, boolean required) {
        return getTypeDeclaration(type, OptionalInt.empty(), required, null);
    }
    String decorateName(String name);
    DatabaseDataType getDataType(String dataType);
    String paging(int from, int count);
}
