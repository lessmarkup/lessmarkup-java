package com.lessmarkup.interfaces.structure;

import com.lessmarkup.interfaces.data.DomainModel;
import java.util.OptionalLong;

public interface NotificationProvider {
    String getTitle();
    String getTooltip();
    String getIcon();
    int getValueChange(OptionalLong fromVersion, OptionalLong toVersion, DomainModel domainModel);
}
