package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

import java.util.OptionalLong;

public class Node extends AbstractDataObject {
    private String path;
    private String title;
    private String description;
    private String handlerId;
    private String settings;
    private boolean enabled;
    private boolean addToMenu;
    private int position;
    private OptionalLong parentId;

    @RequiredField
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    @RequiredField
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @RequiredField
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @RequiredField
    public String getHandlerId() {
        return handlerId;
    }
    public void setHandlerId(String handlerId) {
        this.handlerId = handlerId;
    }

    public String getSettings() {
        return settings;
    }
    public void setSettings(String settings) {
        this.settings = settings;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAddToMenu() {
        return addToMenu;
    }
    public void setAddToMenu(boolean addToMenu) {
        this.addToMenu = addToMenu;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }

    public OptionalLong getParentId() {
        return parentId;
    }
    public void setParentId(OptionalLong parentId) {
        this.parentId = parentId;
    }
}
