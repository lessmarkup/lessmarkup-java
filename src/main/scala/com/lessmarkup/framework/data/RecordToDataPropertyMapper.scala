/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.data

import java.lang.reflect.Method

import com.lessmarkup.interfaces.data.DataObject
import com.lessmarkup.interfaces.exceptions.CommonException
import com.lessmarkup.interfaces.recordmodel.RecordModel

class RecordToDataPropertyMapper(collectionGetter: Method, collectionSetter: Method, modelGetter: Method, modelSetter: Method, fieldType: Class[_]) {

  def getModelValue[T <: RecordModel[_]](thisValue: T): AnyRef = {
    try {
      this.modelGetter.invoke(thisValue)
    }
    catch {
      case ex: Any => throw new CommonException(ex)
    }
  }

  def setModelValue[T <: RecordModel[_]](thisValue: T, newValue: AnyRef) {
    try {
      this.modelSetter.invoke(thisValue, newValue)
    }
    catch {
      case ex: Any => throw new CommonException(ex)
    }
  }

  def getDataValue[T <: DataObject](thisValue: T): AnyRef = {
    try {
      this.collectionGetter.invoke(thisValue)
    }
    catch {
      case ex: Any => throw new CommonException(ex)
    }
  }

  def setDataValue[T <: DataObject](thisValue: T, newValue: AnyRef) {
    try {
      this.collectionSetter.invoke(thisValue, newValue)
    }
    catch {
      case ex: Any => throw new CommonException(ex)
    }
  }

  def getFieldType: Class[_] = {
    fieldType
  }
}