/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.filesystem;

import com.lessmarkup.Constants;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.system.RequestContext;
import com.lessmarkup.interfaces.system.ResourceCache;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import com.samskivert.mustache.Mustache;
import java.io.StringReader;

public class TemplateContext {
    
    private final Mustache.Lambda text;
    private final Mustache.Lambda property;
    private final Mustache.Lambda map;
    
    public static Mustache.Compiler createCompiler(ResourceCache resourceCache) {
        return Mustache.compiler()
                .escapeHTML(false)
                .withDelims("[[ ]]")
                .withLoader(s -> new StringReader(resourceCache.readText(s)));
    }
    
    public TemplateContext(DataCache dataCache) {
        this.text = (frag, out) -> {
            String key = frag.execute();
            out.write(LanguageHelper.getText(Constants.ModuleType.MAIN, key));
        };
        this.property = (frag, out) -> {
            String key = StringHelper.toJsonCase(frag.execute());
            String value;
            switch (key) {
                case "rootPath":
                    value = RequestContextHolder.getContext().getRootPath();
                    break;
                default:
                    value = dataCache.get(SiteConfiguration.class).getProperty(key);
                    break;
            }
            out.write(value);
        };
        RequestContext requestContext = RequestContextHolder.getContext();
        this.map = (frag, out) -> {
            String key = frag.execute();
            String value = requestContext.mapPath(key);
            out.write(value);
        };
    }

    public Mustache.Lambda getText() {
        return text;
    }

    public Mustache.Lambda getProperty() {
        return property;
    }
    
    public Mustache.Lambda getMap() {
        return map;
    }
}
