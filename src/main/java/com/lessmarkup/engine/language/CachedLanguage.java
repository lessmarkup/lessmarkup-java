/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.language;

import com.lessmarkup.interfaces.system.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

public class CachedLanguage implements Language {
    private String name;
    private OptionalLong iconId = OptionalLong.empty();
    private String shortName;
    private boolean isDefault;

    public static class Translation {
        private String reference;
        private String text;

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    private final Map<String, String> translationsMap = new HashMap<>();

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

    @Override
    public boolean getIsDefault() {
        return isDefault;
    }

    @Override
    public Map<String, String> getTranslations() {
        return this.translationsMap;
    }


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
