/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.module

import java.io.{IOException, InputStream}
import java.net.URL

import org.apache.commons.io.IOUtils

trait ModuleConfiguration {
  def getElements: List[String]

  def isSystem: Boolean

  def getUrl: URL

  def getModuleType: String

  def getClassLoader: ClassLoader

  def getInitializer: ModuleInitializer

  @throws(classOf[IOException])
  def getResourceAsStream(path: String): InputStream

  @throws(classOf[IOException])
  def getResourceAsBytes(path: String): Array[Byte] = {
    val stream: InputStream = getResourceAsStream(path)
    try {
      IOUtils.toByteArray(stream)
    } finally {
      if (stream != null) stream.close()
    }
  }
}
