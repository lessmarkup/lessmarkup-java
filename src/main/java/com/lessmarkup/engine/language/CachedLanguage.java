/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.language;

import com.lessmarkup.interfaces.system.Language;
import java.util.Map;
import java.util.OptionalLong;

public class CachedLanguage implements Language {
    private long languageId;
    private String name;
    private OptionalLong iconId;
    private String shortName;
    private boolean isDefault;

    @Override
    public long getLanguageId() {
        return languageId;
    }

    public void setLanguageId(long languageId) {
        this.languageId = languageId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public OptionalLong getIconId() {
        return iconId;
    }

    public void setIconId(OptionalLong iconId) {
        this.iconId = iconId;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Map<String, String> getTranslationsMap() {
        return translationsMap;
    }
    
    public void setTranslationsMap(Map<String, String> translationsMap) {
        this.translationsMap = translationsMap;
    }

    @Override
    public boolean getIsDefault() {
        return isDefault;
    }

    @Override
    public Map<String, String> getTranslations() {
        return this.translationsMap;
    }

    public class Translation {
        private String reference;
        private String text;

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
    
    private Map<String, String> translationsMap;
    
    public String getText(String id) {
        return getText(id, true);
    }
    
    public String getText(String id, boolean throwIfNotFound) {
        String cachedTranslation = translationsMap.get(id);
        if (cachedTranslation == null && throwIfNotFound) {
            throw new IllegalArgumentException();
        }
        return cachedTranslation;
    }
}
