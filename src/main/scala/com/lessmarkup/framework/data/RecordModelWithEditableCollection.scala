/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.data

import com.lessmarkup.framework.helpers.DependencyResolver
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{DataObject, ChangeTracker, DomainModelProvider}
import com.lessmarkup.interfaces.recordmodel.{ModelCollection, RecordModel}

abstract class RecordModelWithEditableCollection[TM <: RecordModel[_], TD <: DataObject]
  (titleTextId: String, dataType: Class[TD], modelType: Class[TM], submitWithCaptcha: Boolean)
  extends RecordModel[TM](titleTextId, null, dataType, submitWithCaptcha) {

  protected def this(dataType: Class[TD], modelType: Class[TM]) {
    this(null, dataType, modelType, false)
  }

  protected def this(titleTextId: String, dataType: Class[TD], modelType: Class[TM]) {
    this(titleTextId, dataType, modelType, false)
  }

  override def createCollection: ModelCollection[TM] = {
    new RecordModelEditableCollection[TM, TD](
      DependencyResolver(classOf[DomainModelProvider]),
      DependencyResolver(classOf[DataCache]),
      DependencyResolver(classOf[ChangeTracker]),
      this.modelType,
      getDataType.asInstanceOf[Class[TD]])
  }
}
