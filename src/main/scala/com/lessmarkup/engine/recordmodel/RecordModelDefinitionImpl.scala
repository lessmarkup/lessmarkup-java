package com.lessmarkup.engine.recordmodel

import java.security.MessageDigest
import java.util._

import com.google.gson.{JsonElement, JsonObject}
import com.lessmarkup.engine.scripting.ScriptHelper
import com.lessmarkup.framework.helpers.{DependencyResolver, LanguageHelper, StringHelper, TypeHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.InputField
import com.lessmarkup.interfaces.data.DataObject
import com.lessmarkup.interfaces.exceptions.RecordValidationException
import com.lessmarkup.interfaces.recordmodel._
import com.lessmarkup.interfaces.system.EngineConfiguration
import com.lessmarkup.{Constants, TextIds}
import net.tanesha.recaptcha.{ReCaptcha, ReCaptchaFactory, ReCaptchaResponse}

import scala.collection.JavaConversions._

object RecordModelDefinitionImpl {
  private val ChallengeFieldKey: String = "-RecaptchaChallenge-"
  private val ResponseFieldKey: String = "-RecaptchaResponse-"
}

class RecordModelDefinitionImpl(modelType: Class[_ <: RecordModel[_]], moduleType: String, index: Int) extends RecordModelDefinition {

  private val recordModelInstance = DependencyResolver.resolve(modelType)
  private val dataType: Class[_ <: DataObject] = recordModelInstance.getDataType
  private val id = createDefinitionId
  private val properties = TypeHelper.getProperties(modelType)
  private val fields = properties
    .map(p => (p, p.getAnnotation(classOf[InputField])))
    .filter(_._2 != null)
    .map { case (p, f) => new InputFieldDefinition(p, f)}
    .toList
  private val columns = properties
    .map(p => (p, p.getAnnotation(classOf[RecordColumn])))
    .filter(_._2 != null)
    .map { case (p, c) =>
      val definition: RecordColumnDefinition = new RecordColumnDefinition
      definition.initialize(c, p)
      if (c.cellTemplate != null) {
        definition.setCellTemplate(c.cellTemplate)
      }
      definition
    }
    .toList

  private def createDefinitionId: String = {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val idString: String = getModelType.getName + index
    val bytes: Array[Byte] = messageDigest.digest(idString.getBytes)
    Base64.getEncoder.encodeToString(bytes)
  }

  def getTitleTextId: String = recordModelInstance.getTitleTextId

  def getModuleType: String = moduleType

  def getModelType: Class[_ <: RecordModel[_]] = modelType

  def getDataType: Class[_ <: DataObject] = dataType

  def getId: String = id

  def getFields = fields

  def getColumns = columns

  def validateInput(objectToValidate: JsonElement, isNew: Boolean) {
    if (objectToValidate == null || objectToValidate.isJsonNull) {
      throw new IllegalArgumentException
    }
    if (isSubmitWithCaptcha) {
      if (!objectToValidate.isJsonObject) {
        throw new RecordValidationException("Cannot validate captcha")
      }
      val propertiesObject: JsonObject = objectToValidate.getAsJsonObject
      val challengeValue: String = propertiesObject.getAsJsonPrimitive(RecordModelDefinitionImpl.ChallengeFieldKey).toString
      val responseValue: String = propertiesObject.getAsJsonPrimitive(RecordModelDefinitionImpl.ResponseFieldKey).toString
      val engineConfiguration: EngineConfiguration = RequestContextHolder.getContext.getEngineConfiguration
      val reCaptcha: ReCaptcha = ReCaptchaFactory.newReCaptcha(engineConfiguration.getRecaptchaPublicKey, engineConfiguration.getRecaptchaPrivateKey, false)
      val response: ReCaptchaResponse = reCaptcha.checkAnswer(RequestContextHolder.getContext.getRemoteAddress, challengeValue, responseValue)
      if (!response.isValid) {
        throw new RecordValidationException(response.getErrorMessage)
      }
    }

    if (!objectToValidate.isJsonObject) {
      return
    }

    fields
      .filter(f => f.isRequired
        && (f.getType != InputFieldType.FILE || !isNew)
        && (StringHelper.isNullOrEmpty(f.getVisibleCondition) || ScriptHelper.evaluateExpression(f.getVisibleCondition, objectToValidate))
        && (StringHelper.isNullOrEmpty(f.getReadOnlyCondition) || !ScriptHelper.evaluateExpression(f.getReadOnlyCondition, objectToValidate))
        && {
          val fieldValue: JsonElement = objectToValidate.getAsJsonObject.get(StringHelper.toJsonCase(f.getProperty.getName))
          fieldValue == null || fieldValue.toString.length == 0
        }
      )
      .foreach(f => {
        val errorText: String = LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.PROPERTY_MUST_BE_SPECIFIED)
        val fieldText: String = if (f.getTextId == null) "" else LanguageHelper.getText(moduleType, f.getTextId)
        throw new RecordValidationException(String.format(errorText, fieldText))
      })
  }

  def isSubmitWithCaptcha: Boolean = recordModelInstance.getSubmitWithCaptcha

  def createModelCollection: ModelCollection[_] = {
    recordModelInstance.createCollection
  }
}