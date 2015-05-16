package com.lessmarkup.userinterface.model.global;

import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.UserGroup;
import com.lessmarkup.framework.data.RecordModelWithEditableCollection;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordColumn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserGroupModel extends RecordModelWithEditableCollection<UserGroupModel, UserGroup> {

    private String name;
    private String description;

    public UserGroupModel() {
        super(TextIds.GROUP, UserGroup.class, UserGroupModel.class);
    }

    @RecordColumn(textId = TextIds.NAME)
    @InputField(textId = TextIds.NAME, type = InputFieldType.TEXT, required = true)
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    @RecordColumn(textId = TextIds.DESCRIPTION)
    @InputField(textId = TextIds.DESCRIPTION, type = InputFieldType.TEXT)
    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }
}
