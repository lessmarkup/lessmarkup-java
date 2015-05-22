/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.language;

import com.google.inject.Inject;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.AbstractCacheHandler;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.module.Implements;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.system.Language;
import com.lessmarkup.interfaces.system.LanguageCache;
import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

@Implements(LanguageCache.class)
public class LanguageCacheImpl extends AbstractCacheHandler implements LanguageCache {

    private final DomainModelProvider domainModelProvider;
    private final ModuleProvider moduleProvider;
    private final Map<String, CachedLanguage> languagesMap = new HashMap<>();
    private String defaultLanguageId = null;
    private final boolean IS_DEBUG = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp");

    @Inject
    public LanguageCacheImpl(DomainModelProvider domainModelProvider, ModuleProvider moduleProvider) {
        super(new Class<?>[]{com.lessmarkup.dataobjects.Language.class});
        this.domainModelProvider = domainModelProvider;
        this.moduleProvider = moduleProvider;
    }

    private static XmlLanguageFile readLanguageFile(String xml) {
        XStream xstream = new XStream();
        xstream.alias("file", XmlLanguageFile.class);
        xstream.alias("translation", XmlTranslation.class);
        xstream.useAttributeFor(XmlLanguageFile.class, "name");
        xstream.useAttributeFor(XmlLanguageFile.class, "id");
        xstream.useAttributeFor(XmlLanguageFile.class, "shortName");
        xstream.useAttributeFor(XmlTranslation.class, "id");
        xstream.useAttributeFor(XmlTranslation.class, "text");

        return (XmlLanguageFile) xstream.fromXML(xml);
    }

    private void loadLanguageFile(byte[] bytes, String moduleType) {

        String xml = StringHelper.binaryToString(bytes);

        XmlLanguageFile file = readLanguageFile(xml);

        if (file == null || file.getTranslations().isEmpty()) {
            return;
        }

        String shortName = file.getShortName().toLowerCase();
        CachedLanguage language = languagesMap.get(shortName);

        if (language == null) {
            language = new CachedLanguage();
            language.setName(file.getName());
            language.setShortName(shortName);
            this.languagesMap.put(shortName, language);
        }

        if (this.defaultLanguageId == null) {
            this.defaultLanguageId = language.getShortName();
        }

        for (XmlTranslation translation : file.getTranslations()) {
            if (translation.getId().length() <= 0) {
                continue;
            }

            String key = LanguageHelper.getFullTextId(moduleType, translation.getId());

            language.getTranslations().put(key, translation.getText());
        }
    }

    @Override
    public void initialize(OptionalLong objectId) {

        for (ModuleConfiguration module : moduleProvider.getModules()) {
            for (String file : module.getElements()) {
                if (file.endsWith(".language.xml")) {
                    try {
                        loadLanguageFile(module.getResourceAsBytes(file), module.getModuleType());
                    } catch (IOException ex) {
                        LoggingHelper.logException(getClass(), ex);
                    }
                }
            }
        }

        try (DomainModel domainModel = domainModelProvider.create()) {

            Map<Long, CachedLanguage> idToLanguage = new HashMap<>();
            List<String> languageIds = new ArrayList<>();

            for (com.lessmarkup.dataobjects.Language language : domainModel.query()
                    .from(com.lessmarkup.dataobjects.Language.class)
                    .where("visible = $", true)
                    .toList(com.lessmarkup.dataobjects.Language.class)) {

                String shortName = language.getShortName().toLowerCase();

                CachedLanguage cachedLanguage = languagesMap.get(shortName);

                if (cachedLanguage == null) {
                    cachedLanguage = new CachedLanguage();
                    cachedLanguage.setShortName(shortName);
                    languagesMap.put(shortName, cachedLanguage);
                }

                idToLanguage.put(language.getId(), cachedLanguage);
                languageIds.add(Long.toString(language.getId()));

                cachedLanguage.setName(language.getName());
                cachedLanguage.setIsDefault(language.getIsDefault());
                cachedLanguage.setIconId(language.getIconId());
            }

            if (!languageIds.isEmpty()) {
                for (com.lessmarkup.dataobjects.Translation translation : domainModel.query()
                        .from(com.lessmarkup.dataobjects.Translation.class)
                        .where("LanguageId in (" + StringHelper.join(",", languageIds) + ")")
                        .toList(com.lessmarkup.dataobjects.Translation.class)) {
                    CachedLanguage language = idToLanguage.get(translation.getLanguageId());
                    if (language == null) {
                        continue;
                    }
                    language.getTranslations().put(translation.getKey(), translation.getText());
                }
            }

        } catch (Exception ex) {
            throw new CommonException(ex);
        }

        Optional<CachedLanguage> defaultLanguage = languagesMap.values().stream().filter(CachedLanguage::getIsDefault).findFirst();
        if (defaultLanguage.isPresent()) {
            defaultLanguageId = defaultLanguage.get().getShortName();
        } else if (!languagesMap.isEmpty()) {
            CachedLanguage language = languagesMap.values().iterator().next();
            defaultLanguageId = language.getShortName();
            language.setIsDefault(true);
        }
    }

    @Override
    public String getCurrentLanguageId() {
        String languageId = RequestContextHolder.getContext().getLanguageId();

        if (languageId == null) {
            return defaultLanguageId;
        }

        CachedLanguage language = languagesMap.get(languageId);

        if (language == null) {
            return defaultLanguageId;
        }

        return languageId;
    }

    @Override
    public String getTranslation(String languageId, String id, String moduleType, boolean throwIfNotFound) {
        if (languageId == null) {
            languageId = defaultLanguageId;
        }
        CachedLanguage language = languageId != null ? languagesMap.get(languageId) : null;
        return getTranslation(language, id, moduleType, throwIfNotFound);
    }

    @Override
    public List<Language> getLanguages() {
        List<Language> ret = new ArrayList<>();
        languagesMap.values().forEach(ret::add);
        return ret;
    }

    private String getTranslation(CachedLanguage language, String id, String moduleType, boolean throwIfNotFound) {

        if (moduleType != null) {
            id = LanguageHelper.getFullTextId(moduleType, id);
        }

        if (language != null) {
            return language.getTranslations().get(id);
        }

        if (!throwIfNotFound) {
            return null;
        }

        if (IS_DEBUG) {
            return "$$-" + id;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
