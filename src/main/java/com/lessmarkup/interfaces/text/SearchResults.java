package com.lessmarkup.interfaces.text;

import java.util.ArrayList;
import java.util.List;

public class SearchResults {
    private final List<SearchResult> results = new ArrayList<>();
    private int actualCount;
    
    public List<SearchResult> getResults() {
        return this.results;
    }
    
    public int getActualCount() {
        return this.actualCount;
    }
    
    public void setActualCount(int val) {
        this.actualCount = val;
    }
}
