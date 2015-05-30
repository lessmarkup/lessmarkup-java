/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.helpers

import java.nio.charset.StandardCharsets

object StringHelper {
  def join(delimiter: String, items: Iterable[String]): String = {
    join(delimiter, items.iterator)
  }

  def getMessage(e: Throwable): String = {
    val message: String = e.getMessage
    if (message != null) {
      return message
    }
    e.toString
  }

  def binaryToString(binary: Seq[Byte]): String = {
    binaryToString(binary.toArray)
  }

  def binaryToString(binary: Array[Byte]): String = {
    if (binary.length >= 3 && binary(0) == -17 && binary(1) == -69 && binary(2) == -65) {
      return new String(binary, 3, binary.length - 3, StandardCharsets.UTF_8)
    }
    new String(binary, StandardCharsets.UTF_8)
  }

  def join(delimiter: String, items: Iterator[String]): String = {
    val ret: StringBuilder = new StringBuilder
    while (items.hasNext) {
      val item: String = items.next()
      if (ret.nonEmpty) {
        ret.append(delimiter)
      }
      ret.append(item)
    }
    ret.toString()
  }

  def toJsonCase(source: String): String = {
    if (source == null || source.length == 0) {
      return source
    }
    source.substring(0, 1).toLowerCase + source.substring(1)
  }

  def fromJsonCase(source: String): String = {
    if (source == null || source.length == 0) {
      return source
    }
    source.substring(0, 1).toUpperCase + source.substring(1)
  }

  def isNullOrEmpty(text: String): Boolean = {
    text == null || text.isEmpty
  }

  def isNullOrWhitespace(text: String): Boolean = {
    if (text == null || text.isEmpty) {
      return true
    }
    text.trim.isEmpty
  }
}
