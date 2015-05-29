/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data

import com.lessmarkup.framework.helpers.TypeHelper
import org.atteo.evo.inflector.English

class TableMetadata(sourceType: Class[_]) {

  private val name: String = English.plural(sourceType.getSimpleName)
  private val columns = TypeHelper.getProperties(sourceType).map(p => (p.getName, p)).toMap

  def getName: String = {
    this.name
  }

  def getColumns = this.columns
}
