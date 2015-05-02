package com.lessmarkup.framework.helpers;

import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.system.LanguageCache;

public final class LanguageHelper {
    
    private static LanguageCache getLanguageCache() {
        return DependencyResolver.resolve(DataCache.class).get(LanguageCache.class);
    }
    
    public static String getText(String moduleType, String id, Object ... args) {
        if (id == null) {
            return null;
        }
        
        String translation = getLanguageCache().getTranslation(id, moduleType);
        
        if (args.length > 0) {
            translation = String.format(translation, args);
        }
        
        return translation;
    }

    public static String getFullTextId(String moduleType, String id) {
        return moduleType + "." + id;
    }
}
