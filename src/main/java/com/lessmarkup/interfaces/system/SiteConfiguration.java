package com.lessmarkup.interfaces.system;

import com.lessmarkup.interfaces.cache.CacheHandler;

public interface SiteConfiguration extends CacheHandler {
    String getSiteName();
    int getRecordsPerPage();
    String getNoReplyEmail();
    String getNoReplyName();
    String getDefaultUserGroup();
    int getMaximumFileSize();
    int getThumbnailWidth();
    int getThumbnailHeight();
    int getMaximumImageWidth();
    boolean getHasUsers();
    boolean getHasNavigationBar();
    boolean getHasSearch();
    boolean getHasLanguages();
    boolean getHasCurrencies();
    String getAdminLoginPage();
    boolean getAdminNotifyNewUsers();
    boolean getAdminApprovesNewUsers();
    String getUserAgreement();
    String getGoogleAnalyticsResource();
    String getValidFileType();
    String getValidFileExtension();
    String getEngineOverride();
    void setEngineOverride(String param);
    String getProperty(String key);
}
