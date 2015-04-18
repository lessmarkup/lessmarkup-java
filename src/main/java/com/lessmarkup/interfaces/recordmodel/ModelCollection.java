/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.interfaces.recordmodel;

import com.lessmarkup.interfaces.data.QueryBuilder;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.OptionalLong;

public interface ModelCollection<T extends RecordModel> {
    List<Long> readIds(QueryBuilder query, boolean ignoreOrder);
    int getCollectionId();
    Collection<T> read(QueryBuilder queryBuilder, List<Long> ids);
    default void initialize(OptionalLong objectId, NodeAccessType nodeAccessType) {
    }
}
