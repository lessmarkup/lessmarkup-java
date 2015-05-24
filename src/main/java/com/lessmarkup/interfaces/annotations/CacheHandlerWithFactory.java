package com.lessmarkup.interfaces.annotations;

import com.lessmarkup.interfaces.cache.CacheHandlerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CacheHandlerWithFactory {
    Class<? extends CacheHandlerFactory> value();
}
