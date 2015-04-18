package com.lessmarkup.interfaces.structure;

import com.lessmarkup.interfaces.recordmodel.InputFieldType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    String textId();
    InputFieldType type();
}
