/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.data

import java.lang.reflect.{Method, Modifier}

import com.lessmarkup.interfaces.cache.AbstractCacheHandler
import com.lessmarkup.interfaces.data.DataObject
import com.lessmarkup.interfaces.recordmodel.RecordModel

import scala.collection.mutable

object EditableCollectionCache {
  private def getTypeMethods(modelType: Class[_]): Map[String, Method] = {
    modelType.getMethods.filter(m => Modifier.isStatic(m.getModifiers)).map(m => (m.getName, m)).toMap
  }
}

class EditableCollectionCache extends AbstractCacheHandler {
  private final val propertySets: mutable.HashMap[(Class[_ <: RecordModel[_]], Class[_ <: DataObject]), List[RecordToDataPropertyMapper]] = mutable.HashMap()

  def getProperties(modelType: Class[_ <: RecordModel[_]], dataType: Class[_ <: DataObject]): List[RecordToDataPropertyMapper] = {
    val key = (modelType, dataType)
    val ret: Option[List[RecordToDataPropertyMapper]] = propertySets.get(key)
    if (ret.isDefined) {
      return ret.get
    }

    val modelMethods: Map[String, Method] = EditableCollectionCache.getTypeMethods(modelType)
    val dataMethods: Map[String, Method] = EditableCollectionCache.getTypeMethods(dataType)

    val properties = modelMethods
      .filter(m => m._1.startsWith("set") && m._2.getParameterCount == 1)
      .map {
        case (setterName, modelSetter) =>
          val fieldType: Class[_] = modelSetter.getParameterTypes()(0)
          var getterName: String = null
          if (fieldType == classOf[Boolean]) {
            getterName = "is" + setterName.substring(3)
          }
          else {
            getterName = "get" + setterName.substring(3)
          }

          val modelGetter: Option[Method] = modelMethods.get(getterName)
          val dataSetter: Option[Method] = dataMethods.get(setterName)
          val dataGetter: Option[Method] = dataMethods.get(getterName)

          if (modelGetter.isEmpty || modelGetter.get.getParameterCount != 0 || !(modelGetter.get.getReturnType == fieldType)
            || dataSetter.isEmpty || dataSetter.get.getParameterCount != 1 || !(dataSetter.get.getParameterTypes()(0) == fieldType)
            || dataGetter.isEmpty || dataGetter.get.getParameterCount != 0 || !(dataGetter.get.getReturnType == fieldType)) {
            None
          } else {
            Option(new RecordToDataPropertyMapper(dataGetter.get, dataSetter.get, modelGetter.get, modelSetter, fieldType))
          }
    }
      .filter(_.isDefined)
      .map(_.get)
      .toList

    propertySets.put(key, properties)
    properties
  }
}
