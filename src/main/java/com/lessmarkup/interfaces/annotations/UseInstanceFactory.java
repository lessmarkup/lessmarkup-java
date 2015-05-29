package com.lessmarkup.interfaces.annotations;

import com.lessmarkup.interfaces.cache.InstanceFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface UseInstanceFactory {
    Class<? extends InstanceFactory> value();
}
