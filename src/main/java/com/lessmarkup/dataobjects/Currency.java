package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

import java.time.OffsetDateTime;

public class Currency extends AbstractDataObject {
    private String name;
    private String code;
    private double rate;
    private boolean enabled;
    private boolean isBase;
    private OffsetDateTime lastUpdate;

    @RequiredField
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @RequiredField
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
    
    public boolean getEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public boolean getIsBase() { return isBase; }
    public void setIsBase(boolean isBase) { this.isBase = isBase; }
    
    public OffsetDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(OffsetDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
