/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.userinterface.model.configuration;

import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.Node;
import com.lessmarkup.dataobjects.NodeAccess;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.dataobjects.UserGroup;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.data.QueryBuilder;
import com.lessmarkup.interfaces.recordmodel.EditableModelCollection;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.ModelCollection;
import com.lessmarkup.interfaces.recordmodel.RecordColumn;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalLong;

@Component
@Scope("prototype")
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
