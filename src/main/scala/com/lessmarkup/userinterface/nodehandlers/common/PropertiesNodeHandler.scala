/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.common

import java.net.URL

import com.google.gson.{JsonArray, JsonObject}
import com.lessmarkup.framework.helpers._
import com.lessmarkup.framework.nodehandlers.{AbstractNodeHandler, NodeHandlerConfiguration}
import com.lessmarkup.interfaces.annotations.{InputFieldType, Property}
import com.lessmarkup.interfaces.module.{ModuleConfiguration, ModuleProvider}

object PropertiesNodeHandler {
  class Property(val name: String, val fieldType: InputFieldType, val value: String)
}

abstract class PropertiesNodeHandler(moduleProvider: ModuleProvider, configuration: NodeHandlerConfiguration, propertiesSource: Seq[PropertiesNodeHandler.Property])
  extends AbstractNodeHandler(configuration) {

  private final val properties: Seq[JsonObject] = propertiesSource.map(p => {
    val model: JsonObject = new JsonObject
    model.addProperty("name", p.name)
    model.addProperty("type", p.fieldType.toString)
    model.addProperty("value", p.value)
    model
  })

  override def getViewType = "properties"

  override def getViewData: Option[JsonObject] = {
    val moduleUrl: URL = getClass.getProtectionDomain.getCodeSource.getLocation
    val moduleConfiguration: Option[ModuleConfiguration] = moduleProvider.getModules.find(m => m.getUrl.equals(moduleUrl))
    val ret: JsonObject = new JsonObject
    val propertiesArray: JsonArray = new JsonArray

    for (
      property <- TypeHelper.getProperties(getClass);
      attribute = property.getAnnotation(classOf[Property]);
      value = property.getValue(this)
    ) {
      val model: JsonObject = new JsonObject
      model.addProperty("name", LanguageHelper.getFullTextId(moduleConfiguration.get.getModuleType, attribute.textId))
      model.addProperty("type", attribute.`type`.toString)
      model.addProperty("value", value.toString)

      if (attribute.`type`() == InputFieldType.IMAGE) {
        val imageId = value.toString.toLong
        model.addProperty("value", ImageHelper.getImageUrl(imageId))
      }

      propertiesArray.add(model)
    }

    properties.foreach(p => propertiesArray.add(p))

    ret.add("properties", propertiesArray)

    Option(ret)
  }
}