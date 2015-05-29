package com.lessmarkup.interfaces.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RecordColumn {
    String width() default "";
    String minWidth() default "";
    String maxWidth() default "";
    boolean visible() default true;
    boolean sortable() default false;
    boolean resizable() default false;
    boolean groupable() default false;
    boolean pinnable() default false;
    String cellClass() default "";
    String cellTemplate() default "";
    String headerClass() default "";
    String textId();
    String cellUrl() default "";
    boolean allowUnsafe() default false;
    String scope() default "";
    RecordColumnAlign align() default RecordColumnAlign.NONE;
    boolean ignoreOptions() default false;
}
