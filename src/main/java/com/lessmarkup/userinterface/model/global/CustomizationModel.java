/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.userinterface.model.global;

import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.SiteCustomization;
import com.lessmarkup.framework.data.RecordModelWithEditableCollection;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.InputFile;
import com.lessmarkup.interfaces.recordmodel.RecordColumn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CustomizationModel extends RecordModelWithEditableCollection<CustomizationModel, SiteCustomization> {
    private boolean typeDefined;
    private byte[] body;
    private boolean binary;
    private String path;
    private boolean append;
    
    public CustomizationModel() {
        super(TextIds.EDIT_CUSTOMIZATION, SiteCustomization.class, CustomizationModel.class);
    }

    @InputField(type = InputFieldType.HIDDEN, defaultValue = "false")
    public void setTypeDefined(boolean typeDefined) { this.typeDefined = typeDefined; }
    public boolean isTypeDefined() { return typeDefined; }

    public void setBody(byte[] body) { this.body = body; }
    public byte[] getBody() { return body; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.IS_BINARY, visibleCondition = "!typeDefined")
    @RecordColumn(textId = TextIds.IS_BINARY)
    public void setBinary(boolean binary) { this.binary = binary; }
    public boolean isBinary() { return binary; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.PATH, required = true)
    @RecordColumn(textId = TextIds.PATH)
    public void setPath(String path) { this.path = path; }
    public String getPath() { return path; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.APPEND, defaultValue = "false", visibleCondition = "!binary")
    public void setAppend(boolean append) { this.append = append; }
    public boolean isAppend() { return append; }

    @InputField(type = InputFieldType.FILE, textId = TextIds.FILE, visibleCondition = "binary", required = true)
    public void setFile(InputFile file) {
        if (file != null && file.getFile() != null && file.getFile().length > 0) {
            body = file.getFile();
        }
    }
    public InputFile getFile() {
        if (!binary) {
            return null;
        }
        
        InputFile ret = new InputFile();
        ret.setFile(body);
        ret.setName("File.bin");
        ret.setType("binary");
        return ret;
    }
    
    @InputField(type = InputFieldType.CODE_TEXT, textId = TextIds.TEXT, visibleCondition = "!binary", required = true)
    public void setText(String text) {
        body = text.getBytes();
    }
    public String getText() {
        if (binary || body == null) {
            return null;
        }
        return new String(body);
    }
}
