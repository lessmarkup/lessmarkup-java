/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.testutilities

import javax.servlet.ServletConfig

import com.lessmarkup.engine.system.EngineConfigurationImpl

class TestEngineConfigurationImpl(servletConfig: ServletConfig) extends EngineConfigurationImpl(servletConfig) {
  override protected def getConfigurationPath(createDirectory: Boolean = false) = ""
}
