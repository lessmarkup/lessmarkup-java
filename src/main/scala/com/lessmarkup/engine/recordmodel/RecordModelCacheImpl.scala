/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.recordmodel

import com.google.inject.Inject
import com.lessmarkup.dataobjects.Language
import com.lessmarkup.interfaces.annotations.Implements
import com.lessmarkup.interfaces.cache.AbstractCacheHandler
import com.lessmarkup.interfaces.module.ModuleProvider
import com.lessmarkup.interfaces.recordmodel.{RecordModelCache, RecordModelDefinition}

import scala.collection.JavaConversions._

@Implements(classOf[RecordModelCache])
class RecordModelCacheImpl @Inject() (moduleProvider: ModuleProvider)
  extends AbstractCacheHandler(Array[Class[_]](classOf[Language]))
  with RecordModelCache {

  private val definitions = createDefinitions
  private val idToDefinitions: Map[String, RecordModelDefinition] = definitions.map(d => (d.getId, d)).toMap
  private val typeToDefinitions: Map[Class[_], RecordModelDefinition] = definitions.map(d => (d.getModelType, d)).toMap

  private def createDefinitions = {

    moduleProvider.getModules
      .flatten(m => m.getInitializer.getModelTypes.map(t => (m, t)))
      .zipWithIndex
      .map { case ((m, t), i) => new RecordModelDefinitionImpl(t, m.getModuleType, i) }
  }

  def getDefinition(modelType: Class[_]): Option[RecordModelDefinition] = {
    typeToDefinitions.get(modelType)
  }

  def getDefinition(id: String): Option[RecordModelDefinition] = {
    idToDefinitions.get(id)
  }

  def hasDefinition(modelType: Class[_]): Boolean = {
    typeToDefinitions.containsKey(modelType)
  }
}
