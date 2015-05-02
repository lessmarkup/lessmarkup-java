package com.lessmarkup.interfaces.recordmodel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface InputField {
    InputFieldType type();
    boolean readOnly() default false;
    String id() default "";
    String textId() default "";
    boolean required() default false;
    double width() default 0;
    int minWidth() default 0;
    int maxWidth() default 0;
    int position() default 0;
    String readOnlyCondition() default "";
    String visibleCondition() default "";
    String enumTextIdBase() default "";
    String defaultValue() default "";
}
