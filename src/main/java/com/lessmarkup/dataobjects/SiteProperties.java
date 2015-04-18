package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

public class SiteProperties extends AbstractDataObject {
    @RequiredField
    private String properties;

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }
}
