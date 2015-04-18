package com.lessmarkup.interfaces.system;

import java.util.OptionalLong;

public interface Language {
    long getLanguageId();
    String getName();
    OptionalLong getIconId();
    String getShortName();
    boolean getIsDefault();
}
