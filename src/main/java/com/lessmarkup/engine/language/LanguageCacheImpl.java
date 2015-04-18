/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.language;

import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.AbstractCacheHandler;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.module.ModuleConfiguration;
import com.lessmarkup.interfaces.module.ModuleProvider;
import com.lessmarkup.interfaces.system.Language;
import com.lessmarkup.interfaces.system.LanguageCache;
import com.thoughtworks.xstream.XStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.logging.Logger;

@Component
@Scope("prototype")
public class LanguageCacheImpl extends AbstractCacheHandler implements LanguageCache {

    private final DomainModelProvider domainModelProvider;
    private final ModuleProvider moduleProvider;
    private final DataCache dataCache;
    private final Map<String, String> defaultTranslations = new HashMap<>();
    private final Map<Long, CachedLanguage> languagesMap = new HashMap<>();
    private final List<CachedLanguage> languagesList = new ArrayList<>();
    private OptionalLong defaultLanguageId = OptionalLong.empty();
    private final Object translationsLock = new Object();
    private final boolean IS_DEBUG = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp");

    @Autowired
    public LanguageCacheImpl(DomainModelProvider domainModelProvider, ModuleProvider moduleProvider, DataCache dataCache) {
        super(new Class<?>[] { com.lessmarkup.dataobjects.Language.class });
        this.domainModelProvider = domainModelProvider;
        this.moduleProvider = moduleProvider;
        this.dataCache = dataCache;
    }
    
    private void loadTranslation(byte[] bytes, String moduleType) {
        
        String xml = StringHelper.binaryToString(bytes);

        XStream xstream = new XStream();
        xstream.alias("file", XmlLanguageFile.class);
        xstream.alias("translation", XmlTranslation.class);
        xstream.useAttributeFor(XmlLanguageFile.class, "name");
        xstream.useAttributeFor(XmlLanguageFile.class, "id");
        xstream.useAttributeFor(XmlLanguageFile.class, "shortName");
        xstream.useAttributeFor(XmlTranslation.class, "id");
        xstream.useAttributeFor(XmlTranslation.class, "text");
        
        XmlLanguageFile file = (XmlLanguageFile) xstream.fromXML(xml);
        
        if (file == null || file.getTranslations().isEmpty()) {
            return;
        }

        file.getTranslations().forEach(translation -> {
            if (translation.getId().length() > 0) {
                String key = moduleType + "." + translation.getId();
                if (defaultTranslations.containsKey(key)) {
                    Logger.getLogger(LanguageCacheImpl.class.getName()).info(String.format("Translation key '%s' already exists", key));
                }
                defaultTranslations.put(key, translation.getText());
            }
        });
    }
    
    @Override
    public void initialize(OptionalLong objectId) {

        for (ModuleConfiguration module : moduleProvider.getModules()) {
            for (String file : module.getElements()) {
                if (file.endsWith(".language.xml")) {
                    try {
                        loadTranslation(module.getResourceAsBytes(file), module.getModuleType());
                    } catch (IOException ex) {
                        LoggingHelper.logException(getClass(), ex);
                    }
                }
            }
        }
        
        try (DomainModel domainModel = domainModelProvider.create()) {
            domainModel.query()
                    .from(com.lessmarkup.dataobjects.Language.class)
                    .where("visible = $", true)
                    .toList(com.lessmarkup.dataobjects.Language.class).forEach(language -> {
                        CachedLanguage cachedLanguage = new CachedLanguage();
                        cachedLanguage.setName(language.getName());
                        cachedLanguage.setIsDefault(language.getIsDefault());
                        cachedLanguage.setIconId(language.getIconId());
                        cachedLanguage.setLanguageId(language.getId());
                        cachedLanguage.setShortName(language.getShortName());
                        languagesMap.put(language.getId(), cachedLanguage);
                        languagesList.add(cachedLanguage);
                    });
        } catch (Exception ex) {
            throw new CommonException(ex);
        }
        
        Optional<CachedLanguage> defaultLanguage = languagesMap.values().stream().filter(l -> l.getIsDefault()).findFirst();
        if (defaultLanguage.isPresent()) {
            defaultLanguageId = OptionalLong.of(defaultLanguage.get().getLanguageId());
        }
    }

    @Override
    public OptionalLong getCurrentLanguageId() {
        OptionalLong languageId = RequestContextHolder.getContext().getLanguageId();
        
        if (!languageId.isPresent()) {
            return defaultLanguageId;
        }
        
        CachedLanguage language = languagesMap.get(languageId.getAsLong());
        
        if (language == null) {
            return defaultLanguageId;
        }
        
        return languageId;
    }
    
    @Override
    public String getTranslation(OptionalLong languageId, String id, String moduleType, boolean throwIfNotFound) {
        CachedLanguage language = languageId.isPresent() ? languagesMap.get(languageId.getAsLong()) : null;
        return getTranslation(language, id, moduleType, throwIfNotFound);
    }

    @Override
    public List<Language> getLanguages() {
        List<Language> ret = new ArrayList<>();
        languagesList.forEach(ret::add);
        return ret;
    }

    @Override
    public Map<String, String> getDefaultTranslations() {
        return defaultTranslations;
    }
    
    private String getTranslation(CachedLanguage language, String id, String moduleType, boolean throwIfNotFound) {
        String translation;
        
        if (moduleType != null) {
            id = moduleType + "." + id;
        }
        
        if (language != null) {
            if (language.getTranslationsMap() == null) {
                synchronized(translationsLock) {
                    if (language.getTranslationsMap() == null) {
                        Map<String, String> translationsMap = new HashMap<>();
                        language.setTranslationsMap(translationsMap);
                        try (DomainModel domainModel = domainModelProvider.create()) {
                            domainModel.query()
                                    .from(com.lessmarkup.dataobjects.Translation.class)
                                    .where("languageId = $", language.getLanguageId())
                                    .toList(com.lessmarkup.dataobjects.Translation.class)
                                    .forEach(t -> {
                                        translationsMap.put(t.getKey(), t.getText());
                                    });
                        } catch (Exception ex) {
                            throw new CommonException(ex);
                        }
                    }
                    
                    translation = language.getText(id, false);
                    
                    if (translation != null) {
                        return translation;
                    }
                }
            }
        }
        
        translation = defaultTranslations.get(id);
        
        if (translation != null) {
            return translation;
        }
        
        if (!throwIfNotFound) {
            return null;
        }
        
        if (IS_DEBUG) {
            return "$$-"+id;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
}
