/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.interfaces.data;

public interface Migrator {
    void executeSql(String sql);
    <T extends DataObject> boolean checkExists(Class<T> type);
    <T extends DataObject> void createTable(Class<T> type);
    default <TD extends DataObject, TB extends DataObject> void addDependency(Class<TD> typeD, Class<TB> typeB) {
        addDependency(typeD, typeB, null);
    }
    <TD extends DataObject, TB extends DataObject> void addDependency(Class<TD> typeD, Class<TB> typeB, String column);
    default <TD extends DataObject, TB extends DataObject> void deleteDependency(Class<TD> typeD, Class<TB> typeB) {
        deleteDependency(typeD, typeB, null);
    }
    <TD extends DataObject, TB extends DataObject> void deleteDependency(Class<TD> typeD, Class<TB> typeB, String column);
    <T extends DataObject> void updateTable(Class<T> type);
    <T extends DataObject> void deleteTable(Class<T> type);
}
