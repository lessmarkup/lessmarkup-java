/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.userinterface.model.configuration;

import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.UserPropertyDefinition;
import com.lessmarkup.dataobjects.UserPropertyType;
import com.lessmarkup.framework.data.RecordModelWithEditableCollection;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordColumn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserPropertyModel extends RecordModelWithEditableCollection<UserPropertyModel, UserPropertyDefinition> {
    private long id;
    private String name;
    private String title;
    private UserPropertyType type;
    
    public UserPropertyModel() {
        super(TextIds.USER_PROPERTY, UserPropertyDefinition.class, UserPropertyModel.class);
    }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.NAME, required = true)
    @RecordColumn(textId = TextIds.NAME)
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.TITLE, required = true)
    @RecordColumn(textId = TextIds.TITLE)
    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    @InputField(type = InputFieldType.SELECT, textId = TextIds.TYPE, required = true)
    @RecordColumn(textId = TextIds.TYPE)
    public void setType(UserPropertyType type) { this.type = type; }
    public UserPropertyType getType() { return type; }
}
