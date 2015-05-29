/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.recordmodel

import com.lessmarkup.interfaces.cache.CacheHandler

trait RecordModelCache extends CacheHandler {
  def getDefinition(recordModelType: Class[_]): Option[RecordModelDefinition]
  def getDefinition(id: String): Option[RecordModelDefinition]
  def hasDefinition(recordModelType: Class[_]): Boolean
}
