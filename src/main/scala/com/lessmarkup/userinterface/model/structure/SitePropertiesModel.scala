/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

import com.google.gson.{JsonElement, JsonObject, JsonPrimitive}
import com.google.inject.Inject
import com.lessmarkup.TextIds
import com.lessmarkup.dataobjects.SiteProperties
import com.lessmarkup.framework.helpers.{JsonSerializer, LoggingHelper, StringHelper, TypeHelper}
import com.lessmarkup.interfaces.annotations.{InputField, InputFieldType}
import com.lessmarkup.interfaces.cache.EntityChangeType
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.system.SiteConfiguration

class SitePropertiesModel @Inject() (domainModelProvider: DomainModelProvider) extends SiteConfiguration {

  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.SITE_NAME)
  var siteName: String = "Site"
  @InputField(fieldType = InputFieldType.NUMBER, textId = TextIds.RECORDS_PER_PAGE)
  var recordsPerPage: Int = 10
  @InputField(fieldType = InputFieldType.EMAIL, textId = TextIds.NO_REPLY_EMAIL)
  var noReplyEmail: String = null
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.NO_REPLY_NAME)
  var noReplyName: String = null
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.DEFAULT_USER_GROUP)
  var defaultUserGroup: String = "Users"
  @InputField(fieldType = InputFieldType.NUMBER, textId = TextIds.MAXIMUM_FILE_SIZE)
  var maximumFileSize: Int = 1024 * 1024 * 10
  @InputField(fieldType = InputFieldType.NUMBER, textId = TextIds.THUMBNAIL_WIDTH)
  var thumbnailWidth: Int = 75
  @InputField(fieldType = InputFieldType.NUMBER, textId = TextIds.THUMBNAIL_HEIGHT)
  var thumbnailHeight: Int = 75
  @InputField(fieldType = InputFieldType.NUMBER, textId = TextIds.MAXIMUM_IMAGE_WIDTH)
  var maximumImageWidth: Int = 800
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.HAS_USERS)
  var hasUsers: Boolean = false
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.HAS_NAVIGATION_BAR)
  var hasNavigationBar: Boolean = false
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.HAS_SEARCH)
  var hasSearch: Boolean = false
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.HAS_LANGUAGES)
  var hasLanguages: Boolean = false
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.HAS_CURRENCIES)
  var hasCurrencies: Boolean = false
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.ADMIN_LOGIN_PAGE)
  var adminLoginPage: String = ""
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.ADMIN_NOTIFY_NEW_USERS)
  var adminNotifyNewUsers: Boolean = false
  @InputField(fieldType = InputFieldType.CHECK_BOX, textId = TextIds.ADMIN_APPROVES_NEW_USERS)
  var adminApprovesNewUsers: Boolean = false
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.USER_AGREEMENT)
  var userAgreement: String = null
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.GOOGLE_ANALYTICS_RESOURCE)
  var googleAnalyticsResource: String = null
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.VALID_FILE_TYPE)
  var validFileType: String = null
  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.VALID_FILE_EXTENSION)
  var validFileExtension: String = null
  var engineOverride: String = null

  def initialize(objectId: Option[Long]) {
    val domainModel: DomainModel = domainModelProvider.create
    try {
      val propertiesDataObject: SiteProperties = domainModel.query.from(classOf[SiteProperties]).first(classOf[SiteProperties], None).get
      if (propertiesDataObject == null || StringHelper.isNullOrEmpty(propertiesDataObject.properties)) {
        return
      }
      val propertiesElement: JsonElement = JsonSerializer.deserializeToTree(propertiesDataObject.properties)
      if (!propertiesElement.isJsonObject) {
        return
      }
      val propertiesObject: JsonObject = propertiesElement.getAsJsonObject
      for (property <- TypeHelper.getProperties(getClass)) {
        val element: JsonPrimitive = propertiesObject.getAsJsonPrimitive(property.getName)
        if (element != null) {
          if (property.getType.equals(classOf[String])) {
            if (element.isString) {
              property.setValue(this, element.getAsString)
            }
          } else if (property.getType.equals(classOf[Int])) {
            if (element.isNumber) {
              property.setValue(this, element.getAsInt.asInstanceOf[AnyRef])
            }
          } else if (property.getType.equals(classOf[Boolean])) {
            if (element.isBoolean) {
              property.setValue(this, element.getAsBoolean.asInstanceOf[AnyRef])
            }
          }
        }
      }
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def save() {
    val propertiesObject: JsonObject = new JsonObject
    for (property <- TypeHelper.getProperties(getClass)) {
      if (property.getType == classOf[String]) {
        propertiesObject.add(property.getName, new JsonPrimitive(property.getValue(this).asInstanceOf[String]))
      }
      else if (property.getType == classOf[Int]) {
        propertiesObject.add(property.getName, new JsonPrimitive(property.getValue(this).asInstanceOf[Int]))
      }
      else if (property.getType == classOf[Boolean]) {
        propertiesObject.add(property.getName, new JsonPrimitive(property.getValue(this).asInstanceOf[Boolean]))
      }
    }
    val domainModel: DomainModel = domainModelProvider.create
    try {
      var propertiesDataObject: SiteProperties = domainModel.query.from(classOf[SiteProperties]).first(classOf[SiteProperties], None).get
      var isNew: Boolean = false
      if (propertiesDataObject == null) {
        isNew = true
        propertiesDataObject = new SiteProperties(
          properties = null
        )
      }
      propertiesDataObject.properties = propertiesObject.toString
      if (isNew) {
        domainModel.create(propertiesDataObject)
      }
      else {
        domainModel.update(propertiesDataObject)
      }
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def expires(collectionId: Int, entityId: Long, changeType: EntityChangeType): Boolean = {
    false
  }

  val handledCollectionTypes: Seq[Class[_]] = Nil

  def isExpired: Boolean = {
    false
  }

  def getProperty(key: String): String = {
    try {
      getClass.getMethod("get" + StringHelper.fromJsonCase(key)).invoke(this).toString
    }
    catch {
      case ex: Any =>
        LoggingHelper.logException(getClass, ex)
        ""
    }
  }
}