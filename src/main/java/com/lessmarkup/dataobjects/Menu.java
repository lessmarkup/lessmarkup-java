package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

import java.time.OffsetDateTime;
import java.util.OptionalLong;

public class Menu extends AbstractDataObject {
    private String text;
    private String description;
    private String argument;
    private String uniqueId;
    private int order;
    private boolean visible;
    private OffsetDateTime created;
    private OffsetDateTime updated;
    private OptionalLong imageId;

    @RequiredField
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    @RequiredField
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getArgument() {
        return argument;
    }
    public void setArgument(String argument) {
        this.argument = argument;
    }

    public String getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getOrder() {
        return order;
    }
    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @RequiredField
    public OffsetDateTime getCreated() {
        return created;
    }
    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }
    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    public OptionalLong getImageId() {
        return imageId;
    }
    public void setImageId(OptionalLong imageId) {
        this.imageId = imageId;
    }
}
