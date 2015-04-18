package com.lessmarkup.userinterface.model.user;

import com.lessmarkup.TextIds;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ChangePasswordModel extends RecordModel<ChangePasswordModel> {

    private String password;

    public ChangePasswordModel() {
        super(TextIds.CHANGE_PASSWORD);
    }

    @InputField(type = InputFieldType.PASSWORD_REPEAT, textId = TextIds.PASSWORD)
    public void setPassword(String password) { this.password = password; }
    public String getPassword() { return password; }
}
