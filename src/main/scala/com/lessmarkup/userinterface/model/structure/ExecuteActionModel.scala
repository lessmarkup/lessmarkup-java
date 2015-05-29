package com.lessmarkup.userinterface.model.structure

import java.net.URLDecoder
import com.google.gson.{JsonElement, JsonObject}
import com.google.inject.Inject
import com.lessmarkup.framework.helpers.{JsonSerializer, StringHelper}
import com.lessmarkup.interfaces.annotations.{ActionAccess, Parameter}
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.structure.NodeCache
import scala.collection.JavaConverters._

class ExecuteActionModel @Inject() (dataCache: DataCache) {

  def handleRequest(data: JsonObject, path: String): JsonObject = {

    val pathDecoded = if (path == null) "" else URLDecoder.decode(path, "UTF-8")
    val queryPost: Int = pathDecoded.indexOf('?')
    val pathExtracted =
      if (queryPost >= 0) {
        pathDecoded.substring(0, queryPost)
      } else {
        pathDecoded
      }
    val nodeCache: NodeCache = this.dataCache.get(classOf[NodeCache])
    val handler = nodeCache.getNodeHandler(pathExtracted)
    val actionName: String = data.get("command").getAsString
    val actionMethod = handler.get.getActionHandler(actionName, data)
    if (actionMethod.isEmpty) {
      throw new IllegalArgumentException
    }
    val actionAccess: ActionAccess = actionMethod.get._2.getAnnotation(classOf[ActionAccess])
    if (actionAccess == null || handler.get.getAccessType.getLevel < actionAccess.minimumAccess.getLevel) {
      throw new IllegalAccessError
    }

    val parameters = actionMethod.get._2.getParameters.toSeq

    def getParameterValue(parameter: java.lang.reflect.Parameter): AnyRef = {
      val attribute = parameter.getAnnotation(classOf[Parameter])
      val parameterName = if (attribute != null) attribute.value else StringHelper.toJsonCase(parameter.getName)
      val parameterType: Class[AnyRef] = parameter.getType.asInstanceOf
      val dataParameter: JsonElement = data.get(parameterName)
      if (dataParameter == null) {
        if (parameterName.startsWith("raw") && (parameterType == classOf[String])) {
          val rawDataParameter = data.get(StringHelper.toJsonCase(parameterName.substring(3)))
          if (rawDataParameter != null) {
            rawDataParameter.toString
          } else {
            null
          }
        } else {
          null
        }
      } else if (classOf[JsonElement].isAssignableFrom(parameterType)) {
        dataParameter
      } else {
        JsonSerializer.deserializePojo(parameterType, dataParameter)
      }
    }

    val argumentsJava = parameters.map(parameter => getParameterValue(parameter)).asJavaCollection
    actionMethod.get._2.invoke(actionMethod.get._1, argumentsJava).asInstanceOf[JsonObject]
  }
}
