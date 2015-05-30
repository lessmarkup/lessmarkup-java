/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.configuration

import com.lessmarkup.TextIds
import com.lessmarkup.framework.helpers.DependencyResolver
import com.lessmarkup.interfaces.annotations.{NodeAccessType, _}
import com.lessmarkup.interfaces.recordmodel._

class NodeAccessModel extends RecordModel[NodeAccessModel] {
  var accessId: Long = 0L
  @InputField(fieldType = com.lessmarkup.interfaces.annotations.InputFieldType.SELECT, textId = TextIds.ACCESS_TYPE, defaultValue = "READ")
  @RecordColumn(textId = TextIds.ACCESS_TYPE)
  var accessType: NodeAccessType = null
  @InputField(fieldType = com.lessmarkup.interfaces.annotations.InputFieldType.TYPEAHEAD, textId = TextIds.USER)
  @RecordColumn(textId = TextIds.USER)
  var user: String = null
  @InputField(fieldType = com.lessmarkup.interfaces.annotations.InputFieldType.TYPEAHEAD, textId = TextIds.GROUP)
  @RecordColumn(textId = TextIds.GROUP)
  var group: String = null

  override def createCollection: ModelCollection[NodeAccessModel] = {
    DependencyResolver(classOf[NodeAccessModelCollectionManager])
  }
}