/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.userinterface.model.configuration;

import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.annotations.InputField;
import com.lessmarkup.interfaces.recordmodel.*;
import com.lessmarkup.interfaces.structure.NodeAccessType;

public class NodeAccessModel extends RecordModel<NodeAccessModel> {
    private long accessId;
    private NodeAccessType accessType;
    private String user;
    private String group;
    
    @Override
    public ModelCollection<NodeAccessModel> createCollection() {
        return DependencyResolver.resolve(NodeAccessModelCollectionManager.class);
    }
    
    public void setAccessId(long accessId) { this.accessId = accessId; }
    public long getAccessId() { return accessId; }
    
    @InputField(type = InputFieldType.SELECT, textId = TextIds.ACCESS_TYPE, defaultValue = "READ")
    @RecordColumn(textId = TextIds.ACCESS_TYPE)
    public void setAccessType(NodeAccessType accessType) { this.accessType = accessType; }
    public NodeAccessType getAccessType() { return accessType; }

    @InputField(type = InputFieldType.TYPEAHEAD, textId = TextIds.USER)
    @RecordColumn(textId = TextIds.USER)
    public void setUser(String user) { this.user = user; }
    public String getUser() { return user; }

    @InputField(type = InputFieldType.TYPEAHEAD, textId = TextIds.GROUP)
    @RecordColumn(textId = TextIds.GROUP)
    public void setGroup(String group) { this.group = group; }
    public String getGroup() { return group; }
}
