package com.lessmarkup.interfaces.recordmodel;

import com.lessmarkup.framework.helpers.PropertyDescriptor;

public class RecordColumnDefinition {
    private String width;
    private String minWidth;
    private String maxWidth;
    private boolean visible;
    private boolean sortable;
    private boolean resizable;
    private boolean groupable;
    private boolean pinnable;
    private String cellClass;
    private String headerClass;
    private String cellTemplate;
    private String textId;
    private PropertyDescriptor property;
    private String cellUrl;
    private boolean allowUnsafe;
    private String scope;
    private RecordColumnAlign align;
    private boolean ignoreOptions;

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(String minWidth) {
        this.minWidth = minWidth;
    }

    public String getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(String maxWidth) {
        this.maxWidth = maxWidth;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public boolean isGroupable() {
        return groupable;
    }

    public void setGroupable(boolean groupable) {
        this.groupable = groupable;
    }

    public boolean isPinnable() {
        return pinnable;
    }

    public void setPinnable(boolean pinnable) {
        this.pinnable = pinnable;
    }

    public String getCellClass() {
        return cellClass;
    }

    public void setCellClass(String cellClass) {
        this.cellClass = cellClass;
    }

    public String getHeaderClass() {
        return headerClass;
    }

    public void setHeaderClass(String headerClass) {
        this.headerClass = headerClass;
    }

    public String getCellTemplate() {
        return cellTemplate;
    }

    public void setCellTemplate(String cellTemplate) {
        this.cellTemplate = cellTemplate;
    }

    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
    }

    public PropertyDescriptor getProperty() {
        return property;
    }

    public void setProperty(PropertyDescriptor property) {
        this.property = property;
    }

    public String getCellUrl() {
        return cellUrl;
    }

    public void setCellUrl(String cellUrl) {
        this.cellUrl = cellUrl;
    }

    public boolean isAllowUnsafe() {
        return allowUnsafe;
    }

    public void setAllowUnsafe(boolean allowUnsafe) {
        this.allowUnsafe = allowUnsafe;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public RecordColumnAlign getAlign() {
        return align;
    }

    public void setAlign(RecordColumnAlign align) {
        this.align = align;
    }

    public boolean isIgnoreOptions() {
        return ignoreOptions;
    }

    public void setIgnoreOptions(boolean ignoreOptions) {
        this.ignoreOptions = ignoreOptions;
    }

    public void initialize(RecordColumn configuration, PropertyDescriptor property) {
        width = configuration.width();
        minWidth = configuration.minWidth();
        maxWidth = configuration.maxWidth();
        visible = configuration.visible();
        sortable = configuration.sortable();
        resizable = configuration.resizable();
        groupable = configuration.groupable();
        pinnable = configuration.pinnable();
        cellClass = configuration.cellClass();
        headerClass = configuration.headerClass();
        textId = configuration.textId();
        this.property = property;
        cellTemplate = configuration.cellTemplate();
        cellUrl = configuration.cellUrl();
        allowUnsafe = configuration.allowUnsafe();
        scope = configuration.scope();
        align = configuration.align();
        ignoreOptions = configuration.ignoreOptions();
    }
}
