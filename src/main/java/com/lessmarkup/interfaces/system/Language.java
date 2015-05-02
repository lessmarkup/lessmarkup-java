package com.lessmarkup.interfaces.system;

import java.util.Map;
import java.util.OptionalLong;

public interface Language {
    String getName();
    OptionalLong getIconId();
    String getShortName();
    boolean getIsDefault();
    Map<String, String> getTranslations();
}
