package com.lessmarkup.interfaces.module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Implements {
    Class<?> value();
}
