package com.lessmarkup.interfaces.text;

import com.lessmarkup.interfaces.cache.CacheHandler;
import com.lessmarkup.interfaces.data.DomainModel;

public interface TextSearch extends CacheHandler {
    SearchResults search(String text, int startRecord, int recordCount, DomainModel domainModel);
}
