/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.helpers

import java.time.OffsetDateTime
import java.util.OptionalLong

import com.google.gson.{JsonElement, JsonNull, JsonObject, JsonParser, JsonPrimitive}

object JsonSerializer {
  def deserializePojo[T <: AnyRef](`type`: Class[T], data: String): Option[T] = {
    deserializePojo(`type`, deserializeToTree(data))
  }

  def serializePojoToTree(`object`: AnyRef): JsonElement = {
    val `type`: Class[_] = `object`.getClass
    if (`type` == classOf[Long]) {
      return new JsonPrimitive(`object`.asInstanceOf[Long])
    }
    else if (`type` == classOf[Int]) {
      return new JsonPrimitive(`object`.asInstanceOf[Int])
    }
    else if (`type` == classOf[String]) {
      return new JsonPrimitive(`object`.asInstanceOf[String])
    }
    else if (classOf[JsonElement].isAssignableFrom(`type`)) {
      return `object`.asInstanceOf[JsonElement]
    }
    val ret: JsonObject = new JsonObject

    for (property <- TypeHelper.getProperties(`object`.getClass)) {
      if (property.getType == classOf[Long]) {
        ret.addProperty(property.getName, property.getValue(`object`).asInstanceOf[Long])
      }
      else if (property.getType == classOf[String]) {
        ret.addProperty(property.getName, property.getValue(`object`).asInstanceOf[String])
      }
      else if (property.getType == classOf[Boolean]) {
        ret.addProperty(property.getName, property.getValue(`object`).asInstanceOf[Boolean])
      }
      else if (property.getType == classOf[Int]) {
        ret.addProperty(property.getName, property.getValue(`object`).asInstanceOf[Int])
      }
      else if (property.getType == classOf[OptionalLong]) {
        val value: OptionalLong = property.getValue(`object`).asInstanceOf[OptionalLong]
        if (value.isPresent) {
          ret.addProperty(property.getName, value.getAsLong)
        }
        else {
          ret.add(property.getName, JsonNull.INSTANCE)
        }
      }
      else if (property.getType == classOf[OffsetDateTime]) {
        ret.addProperty(property.getName, property.getValue(`object`).asInstanceOf[OffsetDateTime].toString)
      }
      else if (classOf[JsonElement].isAssignableFrom(`type`)) {
        ret.add(property.getName, property.getValue(`object`).asInstanceOf[JsonElement])
      }
    }
    ret
  }

  def serializePojo(`object`: AnyRef): String = {
    val obj: JsonElement = serializePojoToTree(`object`)
    obj.toString
  }

  def deserializeToTree(data: String): JsonElement = {
    new JsonParser().parse(data)
  }

  def deserializePojo[T <: AnyRef](dataType: Class[T], data: JsonElement): Option[T] = {

    if (dataType == classOf[Int]) {
      return Option(data.getAsInt.asInstanceOf[Integer].asInstanceOf[T])
    }
    else if (dataType == classOf[Long]) {
      return Option(data.getAsLong.asInstanceOf[T])
    }
    else if (dataType == classOf[String]) {
      return Option(data.getAsString.asInstanceOf[T])
    }
    else if (dataType == classOf[OptionalLong]) {
      if (data.isJsonNull) {
        return Option(OptionalLong.empty.asInstanceOf[T])
      }
      else {
        return Option(OptionalLong.of(data.getAsLong).asInstanceOf[T])
      }
    }
    else if (classOf[JsonElement].isAssignableFrom(dataType)) {
      return Option(data.asInstanceOf[T])
    }

    if (data.isJsonNull || !data.isJsonObject) {
      return None
    }

    val jsonObject: JsonObject = data.getAsJsonObject
    val ret = dataType.newInstance

    TypeHelper.getProperties(dataType)
      .map(p => (p, jsonObject.get(p.getName)))
      .filter(p => p._2 != null)
      .foreach {
      case (property, element) =>
        if (property.getType == classOf[Long]) {
          property.setValue(ret, element.getAsLong.asInstanceOf[AnyRef])
        }
        else if (property.getType == classOf[String]) {
          property.setValue(ret, element.getAsString)
        }
        else if (property.getType == classOf[Boolean]) {
          property.setValue(ret, element.getAsBoolean.asInstanceOf[AnyRef])
        }
        else if (property.getType == classOf[Int]) {
          property.setValue(ret, element.getAsInt.asInstanceOf[AnyRef])
        }
        else if (property.getType == classOf[OptionalLong]) {
          if (!element.isJsonNull) {
            property.setValue(ret, element.getAsLong.asInstanceOf[AnyRef])
          }
        }
        else if (property.getType == classOf[OffsetDateTime]) {
          if (!element.isJsonNull) {
            property.setValue(ret, OffsetDateTime.parse(element.getAsString))
          }
        }
        else if (classOf[JsonElement].isAssignableFrom(property.getType)) {
          property.setValue(ret, element)
        }
    }

    Option(ret)
  }
}