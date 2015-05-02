package com.lessmarkup.interfaces.system;

import com.lessmarkup.interfaces.cache.CacheHandler;
import java.util.List;
import java.util.Map;

public interface LanguageCache extends CacheHandler {
    String getCurrentLanguageId();
    default String getTranslation(String id, String moduleType, boolean throwIfNotFound) {
        return getTranslation(getCurrentLanguageId(), id, moduleType, throwIfNotFound);
    }
    default String getTranslation(String id, String moduleType) {
        return getTranslation(id, moduleType, true);
    }
    String getTranslation(String languageId, String id, String moduleType, boolean throwIfNotFound);
    default String getTranslation(String languageId, String id, String moduleType) {
        return getTranslation(languageId, id, moduleType, true);
    }
    List<Language> getLanguages();
}
