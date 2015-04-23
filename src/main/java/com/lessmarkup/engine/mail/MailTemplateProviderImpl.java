/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.mail;

import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.system.MailTemplateModel;
import com.lessmarkup.interfaces.system.MailTemplateProvider;
import com.lessmarkup.interfaces.system.ResourceCache;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

@Component
@Scope("prototype")
class MailTemplateProviderImpl implements MailTemplateProvider {
    
    private final DataCache dataCache;

    @Autowired
    public MailTemplateProviderImpl(DataCache dataCache) {
        this.dataCache = dataCache;
    }

    @Override
    public <T extends MailTemplateModel> String executeTemplate(Class<T> type, String viewPath, T model) {
        
        ResourceCache resourceCache = dataCache.get(ResourceCache.class);

        Mustache.Compiler compiler = Mustache.compiler();
        Template template = compiler.compile(resourceCache.readText(viewPath));
        return template.execute(model);
    }
}