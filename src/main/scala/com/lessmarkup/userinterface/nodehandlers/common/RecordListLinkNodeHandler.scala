/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.common

import com.lessmarkup.framework.helpers.LanguageHelper
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.DomainModelProvider
import com.lessmarkup.interfaces.recordmodel.RecordModel
import com.lessmarkup.interfaces.structure.{ChildHandlerSettings, NodeHandlerFactory}

object RecordListLinkNodeHandler {
  class CellLinkHandler(val text: String, val handlerType: Class[_ <: NodeHandlerFactory])
}

abstract class RecordListLinkNodeHandler[T <: RecordModel[_]](
    domainModelProvider: DomainModelProvider,
    dataCache: DataCache,
    modelType: Class[T],
    configuration: NodeHandlerConfiguration,
    cellLinkHandlers: Map[String, RecordListLinkNodeHandler.CellLinkHandler] = Map.empty
  )
  extends RecordListNodeHandler[T](domainModelProvider, dataCache, modelType, configuration) {

  override def hasChildren: Boolean = cellLinkHandlers.nonEmpty

  override def createChildHandler(path: Seq[String]): Option[ChildHandlerSettings] = {

    if (path.length < 2) {
      return None
    }

    val recordId = path.head.toLong

    val cellLinkHandler = cellLinkHandlers.get(path(1))
    if (cellLinkHandler.isEmpty) {
      return None
    }

    val localPath = path.drop(2).mkString("/")

    val childHandlerSettings = new NodeHandlerConfiguration(
      objectId = Option(recordId),
      settings = None,
      accessType = getAccessType,
      fullPath = getFullPath + "/" + localPath,
      path = localPath
    )

    val handler = createChildHandler(cellLinkHandler.get.handlerType, childHandlerSettings)

    Option(new ChildHandlerSettings(
      handler = handler,
      id = childHandlerSettings.objectId,
      title = LanguageHelper.getFullTextId(getRecordModel.getModuleType, cellLinkHandler.get.text),
      path = localPath,
      rest = path.drop(2)
    ))
  }
}
