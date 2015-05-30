/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.system

import com.lessmarkup.interfaces.cache.CacheHandler

trait ResourceCache extends CacheHandler {
  def resourceExists(path: String): Boolean
  def readBytes(path: String): Option[Seq[Byte]]
  def readText(path: String): Option[String]
  def parseText(path: String): Option[String]
}