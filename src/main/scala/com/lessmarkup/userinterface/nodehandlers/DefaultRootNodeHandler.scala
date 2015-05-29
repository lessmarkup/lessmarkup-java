/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers

import com.lessmarkup.framework.nodehandlers.{AbstractNodeHandler, NodeHandlerConfiguration}
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}

class DefaultRootNodeHandlerFactory extends NodeHandlerFactory {
  override def createNodeHandler(nodeHandlerConfiguration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    new DefaultRootNodeHandler(nodeHandlerConfiguration)
  }
}

class DefaultRootNodeHandler(configuration: NodeHandlerConfiguration) extends AbstractNodeHandler(configuration) {
  override def isStatic: Boolean = true
}
