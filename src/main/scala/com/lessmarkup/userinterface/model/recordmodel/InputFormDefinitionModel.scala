package com.lessmarkup.userinterface.model.recordmodel

import com.google.gson.{JsonArray, JsonElement, JsonObject}
import com.google.inject.Inject
import com.lessmarkup.framework.helpers.{DependencyResolver, LanguageHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.{InputFieldType, UseInstanceFactory}
import com.lessmarkup.interfaces.cache.{DataCache, InstanceFactory}
import com.lessmarkup.interfaces.recordmodel.{InputSource, RecordModelCache, RecordModelDefinition, SelectValueModel}
import com.lessmarkup.interfaces.system.EngineConfiguration

class InputFormDefinitionModelFactory @Inject() (dataCache: DataCache) extends InstanceFactory {
  override def createInstance(params: Any*): AnyRef = {

    val id = params.head

    val recordModelCache: RecordModelCache = dataCache.get(classOf[RecordModelCache])

    val definition = id match {
      case id: String =>
        recordModelCache.getDefinition(id).get
      case id: Class[_] =>
        recordModelCache.getDefinition(id).get
      case _ => throw new IllegalArgumentException
    }

    new InputFormDefinitionModel(dataCache, definition)
  }
}

@UseInstanceFactory(classOf[InputFormDefinitionModelFactory])
class InputFormDefinitionModel(dataCache: DataCache, definition: RecordModelDefinition) {

  private final val fields: List[InputFieldModel] = {
    if (definition == null) {
      List()
    } else {
      val inputSource = DependencyResolver.resolve(definition.getModelType).asInstanceOf[InputSource]
      for (source <- definition.getFields) yield {

        val selectValues: Seq[SelectValueModel] =
          if (source.getEnumValues != null && source.getEnumValues.nonEmpty) {
            for (value <- source.getEnumValues) yield {
              new SelectValueModel(LanguageHelper.getFullTextId(definition.getModuleType, value.getTextId), value.getValue)
            }
          }
          else if (source.getType == InputFieldType.SELECT || source.getType == InputFieldType.SELECT_TEXT || source.getType == InputFieldType.MULTI_SELECT) {
            inputSource.getEnumValues(source.getProperty.getName)
              .map(es => new SelectValueModel(es.getText, es.getValue))
          } else {
            Seq()
          }

        new InputFieldModel(source, definition, selectValues)
      }
    }
  }

  private val title: String = LanguageHelper.getText(definition.getModuleType, definition.getTitleTextId)

  private val submitWithCaptcha: Boolean = {
    if (definition.isSubmitWithCaptcha) {
      val engineConfiguration: EngineConfiguration = RequestContextHolder.getContext.getEngineConfiguration
      val privateKey: String = engineConfiguration.getRecaptchaPrivateKey
      val publicKey: String = engineConfiguration.getRecaptchaPublicKey
      if (privateKey != null && privateKey.length > 0 && publicKey != null && publicKey.length > 0) {
        true
      } else {
        false
      }
    } else {
      false
    }
  }

  def getFields: Seq[InputFieldModel] = fields

  def toJson: JsonElement = {
    val ret: JsonObject = new JsonObject
    ret.addProperty("title", this.title)
    ret.addProperty("submitWithCaptcha", this.submitWithCaptcha)
    val fieldsArray: JsonArray = new JsonArray
    for (model <- fields) {
      fieldsArray.add(model.toJson)
    }
    ret.add("fields", fieldsArray)
    ret
  }
}