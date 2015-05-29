/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.common

import com.google.gson.{JsonNull, JsonObject}
import com.lessmarkup.framework.helpers.{DependencyResolver, JsonSerializer, LanguageHelper}
import com.lessmarkup.framework.nodehandlers.{AbstractNodeHandler, NodeHandlerConfiguration}
import com.lessmarkup.interfaces.annotations.{ActionAccess, InputFieldType, Parameter}
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.recordmodel.{RecordModelCache, RecordModelDefinition}
import com.lessmarkup.userinterface.model.recordmodel.InputFormDefinitionModel
import com.lessmarkup.{Constants, TextIds}

abstract class DialogNodeHandler[T <: AnyRef](
  dataCache: DataCache,
  modelType: Class[T],
  configuration: NodeHandlerConfiguration
) extends AbstractNodeHandler(configuration) {

  private final val definitionModel: InputFormDefinitionModel = DependencyResolver.resolve(classOf[InputFormDefinitionModel], modelType)

  override def getScripts: Seq[String] = {
    super.getScripts ++ definitionModel.getFields.flatMap(f => {
      f.getType match {
        case InputFieldType.CODE_TEXT => Option("scripts/directives/CodeMirrorDirective")
        case InputFieldType.RICH_TEXT => Option("scripts/directives/CkEditorDirective")
        case _ => None
      }
    })
  }

  protected def loadObject: Option[T]

  protected def saveObject(changedObject: Option[T]): String

  protected def getApplyCaption: String = {
    LanguageHelper.getFullTextId(Constants.ModuleTypeMain, TextIds.APPLY_BUTTON)
  }

  override def getViewData: Option[JsonObject] = {
    val ret: JsonObject = new JsonObject
    ret.add("definition", definitionModel.toJson)
    val source = loadObject
    if (source.isEmpty) {
      ret.add("object", JsonNull.INSTANCE)
    }
    else {
      ret.add("object", JsonSerializer.serializePojoToTree(source.get))
    }
    ret.addProperty("applyCaption", getApplyCaption)
    Option(ret)
  }

  @ActionAccess
  def save(@Parameter("changedObject") changedObject: JsonObject): JsonObject = {
    val model: RecordModelDefinition = dataCache.get(classOf[RecordModelCache]).getDefinition(modelType).get
    model.validateInput(changedObject, isNew = false)
    var message: String = saveObject(JsonSerializer.deserializePojo(modelType, changedObject))
    if (message == null) {
      message = LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.SUCCESSFULLY_SAVED)
    }
    val ret: JsonObject = new JsonObject
    ret.addProperty("message", message)
    ret
  }

  override def getViewType = "dialog"
}
