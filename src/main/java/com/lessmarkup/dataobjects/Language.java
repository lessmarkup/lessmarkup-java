package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

import java.util.OptionalLong;

public class Language extends AbstractDataObject {
    private String name;
    private OptionalLong iconId;
    private String shortName;
    private boolean visible;
    private boolean isDefault;

    @RequiredField
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public OptionalLong getIconId() {
        return iconId;
    }
    public void setIconId(OptionalLong iconId) {
        this.iconId = iconId;
    }

    @RequiredField
    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean isVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getIsDefault() {
        return isDefault;
    }
    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
