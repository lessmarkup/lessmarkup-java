/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.interfaces.recordmodel;

import java.util.Collection;

public interface EditableModelCollection<T extends RecordModel> extends ModelCollection<T> {
        T createRecord();
        void addRecord(T record);
        void updateRecord(T record);
        boolean deleteRecords(Collection<Long> recordIds);
        default boolean isDeleteOnly() { return false; }
}
