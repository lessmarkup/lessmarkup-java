package com.lessmarkup.engine.currencies;

public class CurrencyCacheItem {
    private final long currencyId;
    private final String name;
    private final String code;
    private final double rate;
    private final boolean isBase;
    
    public CurrencyCacheItem(long currencyId, String name, String code, double rate, boolean isBase) {
        this.currencyId = currencyId;
        this.name = name;
        this.code = code;
        this.rate = rate;
        this.isBase = isBase;
    }
    
    public boolean isBase() {
        return this.isBase;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getCode() {
        return this.code;
    }
    
    public double getRate() {
        return this.rate;
    }
    
    public long getCurrencyId() {
        return this.currencyId;
    }
}
