package com.lessmarkup.interfaces.module;

import java.util.Collection;

public interface UserPropertyProvider {
    Collection<UserProperty> getProperties(long userId);
}
