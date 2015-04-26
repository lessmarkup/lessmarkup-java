package com.lessmarkup.userinterface.model.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.SiteProperties;
import com.lessmarkup.framework.helpers.JsonSerializer;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.helpers.PropertyDescriptor;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.helpers.TypeHelper;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.OptionalLong;

@Component
@Scope("prototype")
public class SitePropertiesModel implements SiteConfiguration {

    private final DomainModelProvider domainModelProvider;
    private final ChangeTracker changeTracker;

    private String siteName = "Site";
    private int recordsPerPage = 10;
    private String noReplyEmail = null;
    private String noReplyName = null;
    private String defaultUserGroup = "Users";
    private int maximumFileSize = 1024 * 1024 * 10;
    private int thumbnailWidth = 75;
    private int thumbnailHeight = 75;
    private int maximumImageWidth = 800;
    private boolean hasUsers = false;
    private boolean hasNavigationBar = false;
    private boolean hasSearch = false;
    private boolean hasLanguages = false;
    private boolean hasCurrencies = false;
    private String adminLoginPage = "";
    private boolean adminNotifyNewUsers = false;
    private boolean adminApprovesNewUsers = false;
    private String userAgreement = null;
    private String googleAnalyticsResource = null;
    private String validFileType = null;
    private String validFileExtension = null;
    private String engineOverride = null;

    @Autowired
    public SitePropertiesModel(DomainModelProvider domainModelProvider, ChangeTracker changeTracker) {
        this.domainModelProvider = domainModelProvider;
        this.changeTracker = changeTracker;
    }


    @InputField(type = InputFieldType.TEXT, textId = TextIds.SITE_NAME)
    public void setSiteName(String siteName) { this.siteName = siteName; }
    @Override public String getSiteName() { return this.siteName; }

    @InputField(type = InputFieldType.NUMBER, textId = TextIds.RECORDS_PER_PAGE)
    public void setRecordsPerPage(int recordsPerPage) { this.recordsPerPage = recordsPerPage; }
    @Override public int getRecordsPerPage() { return this.recordsPerPage; }

    @InputField(type = InputFieldType.EMAIL, textId = TextIds.NO_REPLY_EMAIL)
    public void setNoReplyEmail(String noReplyEmail) { this.noReplyEmail = noReplyEmail; }
    @Override public String getNoReplyEmail() { return this.noReplyEmail; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.NO_REPLY_NAME)
    public void setNoReplyName(String noReplyName) { this.noReplyName = noReplyName; }
    @Override public String getNoReplyName() { return this.noReplyName; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.DEFAULT_USER_GROUP)
    public void setDefaultUserGroup(String defaultUserGroup) { this.defaultUserGroup = defaultUserGroup; }
    @Override public String getDefaultUserGroup() { return this.defaultUserGroup; }

    @InputField(type = InputFieldType.NUMBER, textId = TextIds.MAXIMUM_FILE_SIZE)
    public void setMaximumFileSize(int maximumFileSize) { this.maximumFileSize = maximumFileSize; }
    @Override public int getMaximumFileSize() { return this.maximumFileSize; }

    @InputField(type = InputFieldType.NUMBER, textId = TextIds.THUMBNAIL_WIDTH)
    public void setThumbnailWidth(int thumbnailWidth) { this.thumbnailWidth = thumbnailWidth; }
    @Override public int getThumbnailWidth() { return this.thumbnailWidth; }

    @InputField(type = InputFieldType.NUMBER, textId = TextIds.THUMBNAIL_HEIGHT)
    public void setThumbnailHeight(int thumbnailHeight) { this.thumbnailHeight = thumbnailHeight; }
    @Override public int getThumbnailHeight() { return this.thumbnailHeight; }

    @InputField(type = InputFieldType.NUMBER, textId = TextIds.MAXIMUM_IMAGE_WIDTH)
    public void setMaximumImageWidth(int maximumImageWidth) { this.maximumImageWidth = maximumImageWidth; }
    @Override public int getMaximumImageWidth() { return this.maximumImageWidth; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.HAS_USERS)
    public void setHasUsers(boolean hasUsers) { this.hasUsers = hasUsers; }
    @Override public boolean getHasUsers() { return this.hasUsers; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.HAS_NAVIGATION_BAR)
    public void setHasNavigationBar(boolean hasNavigationBar) { this.hasNavigationBar = hasNavigationBar; }
    @Override public boolean getHasNavigationBar() { return this.hasNavigationBar; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.HAS_SEARCH)
    public void setHasSearch(boolean hasSearch) { this.hasSearch = hasSearch; }
    @Override public boolean getHasSearch() { return this.hasSearch; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.HAS_LANGUAGES)
    public void setHasLanguages(boolean hasLanguages) { this.hasLanguages = hasLanguages; }
    @Override public boolean getHasLanguages() { return this.hasLanguages; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.HAS_CURRENCIES)
    public void setHasCurrencies(boolean hasCurrencies) { this.hasCurrencies = hasCurrencies; }
    @Override public boolean getHasCurrencies() { return this.hasCurrencies; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.ADMIN_LOGIN_PAGE)
    public void setAdminLoginPage(String adminLoginPage) { this.adminLoginPage = adminLoginPage; }
    @Override public String getAdminLoginPage() { return this.adminLoginPage; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.ADMIN_NOTIFY_NEW_USERS)
    public void setAdminNotifyNewUsers(boolean adminNotifyNewUsers) { this.adminNotifyNewUsers = adminNotifyNewUsers; }
    @Override public boolean getAdminNotifyNewUsers() { return this.adminNotifyNewUsers; }

    @InputField(type = InputFieldType.CHECK_BOX, textId = TextIds.ADMIN_APPROVES_NEW_USERS)
    public void setAdminApprovesNewUsers(boolean adminApprovesNewUsers) { this.adminApprovesNewUsers = adminApprovesNewUsers; }
    @Override public boolean getAdminApprovesNewUsers() { return this.adminApprovesNewUsers; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.USER_AGREEMENT)
    public void setUserAgreement(String userAgreement) { this.userAgreement = userAgreement; }
    @Override public String getUserAgreement() { return this.userAgreement; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.GOOGLE_ANALYTICS_RESOURCE)
    public void setGoogleAnalyticsResource(String googleAnalyticsResource) { this.googleAnalyticsResource = googleAnalyticsResource; }
    @Override public String getGoogleAnalyticsResource() { return this.googleAnalyticsResource; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.VALID_FILE_TYPE)
    public void setValidFileType(String validFileType) { this.validFileType = validFileType; }
    @Override public String getValidFileType() { return this.validFileType; }

    @InputField(type = InputFieldType.TEXT, textId = TextIds.VALID_FILE_EXTENSION)
    public void setValidFileExtension(String validFileExtension) { this.validFileExtension = validFileExtension; }
    @Override public String getValidFileExtension() { return this.validFileExtension; }

    @Override public void setEngineOverride(String param) { this.engineOverride = param; }
    @Override public String getEngineOverride() { return this.engineOverride; }

    @Override
    public void initialize(OptionalLong objectId) {
        try (DomainModel domainModel = domainModelProvider.create()) {
            SiteProperties propertiesDataObject = domainModel.query().from(SiteProperties.class).firstOrDefault(SiteProperties.class);
            if (propertiesDataObject == null || StringHelper.isNullOrEmpty(propertiesDataObject.getProperties())) {
                return;
            }

            JsonElement propertiesElement = JsonSerializer.deserializeToTree(propertiesDataObject.getProperties());
            
            if (!propertiesElement.isJsonObject()) {
                return;
            }
            
            JsonObject propertiesObject = propertiesElement.getAsJsonObject();
            for (PropertyDescriptor property : TypeHelper.getProperties(getClass())) {
                JsonPrimitive element = propertiesObject.getAsJsonPrimitive(property.getName());
                if (element == null) {
                    continue;
                }
                if (property.getType().equals(String.class)) {
                    if (element.isString()) {
                        property.setValue(this, element.getAsString());
                    }
                } else if (property.getType().equals(int.class)) {
                    if (element.isNumber()) {
                        property.setValue(this, element.getAsInt());
                    }
                } else if (property.getType().equals(boolean.class)) {
                    if (element.isBoolean()) {
                        property.setValue(this, element.getAsBoolean());
                    }
                }
            }
        }
    }

    public void save() {
        JsonObject propertiesObject = new JsonObject();
        for (PropertyDescriptor property : TypeHelper.getProperties(getClass())) {
            if (property.getType().equals(String.class)) {
                propertiesObject.add(property.getName(), new JsonPrimitive((String) property.getValue(this)));
            } else if (property.getType().equals(int.class)) {
                propertiesObject.add(property.getName(), new JsonPrimitive((int) property.getValue(this)));
            } else if (property.getType().equals(boolean.class)) {
                    propertiesObject.add(property.getName(), new JsonPrimitive((boolean) property.getValue(this)));
            }
        }

        try (DomainModel domainModel = domainModelProvider.create()) {
            SiteProperties propertiesDataObject = domainModel.query().from(SiteProperties.class).firstOrDefault(SiteProperties.class);
            boolean isNew = false;
            if (propertiesDataObject == null) {
                isNew = true;
                propertiesDataObject = new SiteProperties();
            }
            propertiesDataObject.setProperties(propertiesObject.toString());
            if (isNew) {
                domainModel.create(propertiesDataObject);
            } else {
                domainModel.update(propertiesDataObject);
            }
        }
    }

    @Override
    public boolean expires(int collectionId, long entityId, EntityChangeType changeType) {
        return false;
    }

    @Override
    public Collection<Class<?>> getHandledCollectionTypes() {
        return null;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public String getProperty(String key) {
        try {
            return getClass().getMethod("get" + StringHelper.fromJsonCase(key)).invoke(this).toString();
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LoggingHelper.logException(getClass(), ex);
            return "";
        }
    }
}
