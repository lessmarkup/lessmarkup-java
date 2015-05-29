/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.module

import com.lessmarkup.interfaces.data.DomainModelProvider
import com.lessmarkup.interfaces.structure.NodeHandlerFactory

trait ModuleProvider {
  def getModules: Seq[ModuleConfiguration]
  def updateModuleDatabase(domainModelProvider: DomainModelProvider)
  def getNodeHandlers: Seq[String]
  def getNodeHandler(id: String): Option[(Class[_ <: NodeHandlerFactory], String)]
}
