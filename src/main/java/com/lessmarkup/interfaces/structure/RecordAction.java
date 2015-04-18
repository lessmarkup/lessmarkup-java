package com.lessmarkup.interfaces.structure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;

@Retention(RetentionPolicy.RUNTIME)
public @interface RecordAction {
    NodeAccessType minimumAccess() default NodeAccessType.NO_ACCESS;
    String nameTextId();
    String visible() default "";
    Class<?> createType() default Object.class;
    boolean initialize() default false;
}
