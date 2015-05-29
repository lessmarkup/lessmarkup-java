/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.helpers

import java.lang.annotation.Annotation
import java.lang.reflect.{InvocationTargetException, Method}

import com.lessmarkup.interfaces.exceptions.CommonException

class PropertyDescriptor(name: String, getter: Method, setter: Method, dataType: Class[_]) {

  def getValue(thisObject: AnyRef): AnyRef = {
    try {
      getter.invoke(thisObject)
    }
    catch {
      case ex: InvocationTargetException => throw new CommonException(ex.getTargetException)
      case ex: Any => throw new CommonException(ex)
    }
  }

  def setValue(thisObject: AnyRef, newValue: AnyRef) {
    try {
      setter.invoke(thisObject, newValue)
    }
    catch {
      case ex: InvocationTargetException => throw new CommonException(ex.getTargetException)
      case ex: Any => throw new CommonException(ex)
    }
  }

  def getName: String = {
    name
  }

  def getType: Class[_] = dataType

  def getAnnotation[T <: Annotation](annotationClass: Class[T]): T = {
    val ret: T = getter.getAnnotation(annotationClass)
    if (ret != null) {
      return ret
    }
    setter.getAnnotation(annotationClass)
  }
}