/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.structure

import com.lessmarkup.interfaces.cache.CacheHandler

trait NodeCache extends CacheHandler {
  def getNode(nodeId: Long): Option[CachedNodeInformation]

  def getNode(path: String): Option[(CachedNodeInformation, String)]

  def getRootNode: Option[CachedNodeInformation]

  def getNodes: Seq[CachedNodeInformation]

  def getNodeHandler(path: String, filter: Option[(NodeHandler, String, String, Seq[String], Option[Long]) => Boolean] = None): Option[NodeHandler]

  def getNodeHandler(path: String): Option[NodeHandler] = {
    getNodeHandler(path, null)
  }
}
