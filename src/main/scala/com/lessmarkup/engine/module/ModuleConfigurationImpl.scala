/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.module

import java.io.InputStream
import java.net.URL

import com.lessmarkup.interfaces.module.{ModuleConfiguration, ModuleInitializer}

class ModuleConfigurationImpl(
                               url: URL,
                               system: Boolean,
                               moduleType: String,
                               elements: List[String],
                               classLoader: ClassLoader,
                               moduleInitializer: ModuleInitializer) extends ModuleConfiguration {

  def getElements: List[String] = {
    elements
  }

  def isSystem: Boolean = {
    this.system
  }

  def getUrl: URL = {
    this.url
  }

  def getModuleType: String = {
    this.moduleType
  }

  def getClassLoader: ClassLoader = {
    this.classLoader
  }

  def getInitializer: ModuleInitializer = {
    this.moduleInitializer
  }

  def getResourceAsStream(path: String): InputStream = {
    getClassLoader.getResourceAsStream(path)
  }
}