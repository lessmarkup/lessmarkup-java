/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.helpers

import java.lang.reflect.Modifier
import scala.collection.concurrent

object TypeHelper {

  private val propertyMap: concurrent.Map[Class[_], Seq[PropertyDescriptor]] = concurrent.TrieMap()

  def getProperties(dataType: Class[_]): Seq[PropertyDescriptor] = {

    val ret = propertyMap.get(dataType)
    if (ret.isDefined) {
      return ret.get
    }

    val methods = dataType.getMethods
      .filter(m => !Modifier.isStatic(m.getModifiers))
      .map(m => (m.getName, m))
      .toMap

    val properties: Seq[PropertyDescriptor] = (for (
      (key, setter) <- methods
      if key.endsWith("_$eq") && setter.getParameterCount == 1;
      fieldType = setter.getParameterTypes()(0);
      propertyName = key.substring(0, key.length-4);
      getter = methods.get(propertyName)
      if getter.isDefined
    ) yield new PropertyDescriptor(StringHelper.toJsonCase(propertyName), getter.get, setter, fieldType)
    ).toSeq

    propertyMap.put(dataType, properties)
    properties
  }
}
