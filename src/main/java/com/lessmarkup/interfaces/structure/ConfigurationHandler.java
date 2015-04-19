package com.lessmarkup.interfaces.structure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationHandler {
    String titleTextId();
}
