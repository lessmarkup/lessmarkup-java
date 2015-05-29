/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.recordmodel

import com.lessmarkup.framework.helpers.PropertyDescriptor
import com.lessmarkup.interfaces.annotations.{RecordColumnAlign, RecordColumn}

class RecordColumnDefinition(configuration: RecordColumn, var property: PropertyDescriptor) {
  val width:String = configuration.width
  val minWidth: String = configuration.minWidth
  val maxWidth: String = configuration.maxWidth
  val visible: Boolean = configuration.visible
  val sortable: Boolean = configuration.sortable
  val resizable: Boolean = configuration.resizable
  val groupable: Boolean = configuration.groupable
  val pinnable: Boolean = configuration.pinnable
  val cellClass: String = configuration.cellClass
  val headerClass: String = configuration.headerClass
  val cellTemplate: String = configuration.cellTemplate
  val textId: String = configuration.textId
  val cellUrl: String = configuration.cellUrl
  val allowUnsafe: Boolean = configuration.allowUnsafe
  val scope: String = configuration.scope
  val align: RecordColumnAlign = configuration.align
  val ignoreOptions: Boolean = configuration.ignoreOptions
}