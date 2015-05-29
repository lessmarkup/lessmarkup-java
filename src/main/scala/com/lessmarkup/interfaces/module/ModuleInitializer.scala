/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.module

import com.lessmarkup.interfaces.data.{DataObject, Migration}
import com.lessmarkup.interfaces.recordmodel.RecordModel
import com.lessmarkup.interfaces.structure.NodeHandlerFactory

trait ModuleInitializer {
  def initialize() {
  }

  def getName: String
  def getModuleType: String
  def getModelTypes: Seq[Class[_ <: RecordModel[_]]]
  def getDataObjectTypes: Seq[Class[_ <: DataObject]]
  def getNodeHandlerTypes: Seq[Class[_ <: NodeHandlerFactory]]
  def getMigrations: Seq[Class[_ <: Migration]]
}