package com.lessmarkup.interfaces.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionAccess {
    NodeAccessType minimumAccess() default NodeAccessType.NO_ACCESS;
}
