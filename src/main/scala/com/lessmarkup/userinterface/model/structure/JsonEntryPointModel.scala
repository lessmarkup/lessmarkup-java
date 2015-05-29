/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

import java.io.{InputStreamReader, OutputStream, Reader}

import com.google.gson.{JsonElement, JsonObject, JsonParser, JsonPrimitive}
import com.google.inject.Inject
import com.lessmarkup.framework.helpers.{DependencyResolver, LoggingHelper, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{ChangeTracker, ChangesCache}
import com.lessmarkup.interfaces.security.CurrentUser
import com.lessmarkup.interfaces.system.RequestContext
import com.lessmarkup.userinterface.model.common.SearchTextModel
import com.lessmarkup.userinterface.model.recordmodel.InputFormDefinitionModel
import com.lessmarkup.userinterface.model.user.{LoginModel, RegisterModel}

import scala.collection.JavaConversions._

object JsonEntryPointModel {
  def appliesToRequest: Boolean = {
    RequestContextHolder.getContext.isJsonRequest
  }
}

class JsonEntryPointModel @Inject() (dataCache: DataCache, changeTracker: ChangeTracker) {

  private var nodeId: Option[Long] = None

  private def getRequestData: JsonObject = {
    val reader: Reader = new InputStreamReader(RequestContextHolder.getContext.getInputStream)
    try {
      val parser: JsonParser = new JsonParser
      val element: JsonElement = parser.parse(reader)
      if (!element.isJsonObject) {
        return null
      }
      element.getAsJsonObject
    } finally {
      if (reader != null) reader.close()
    }
  }

  private def isUserVerified: Boolean = {
    val currentUser: CurrentUser = RequestContextHolder.getContext.getCurrentUser
    currentUser.isAdministrator || (currentUser.emailConfirmed && currentUser.isApproved)
  }

  private def handleDataRequest(data: JsonObject, command: String, path: String): JsonElement = {
    StringHelper.toJsonCase(command) match {
      case "form" =>
        val model: InputFormDefinitionModel = DependencyResolver.resolve(classOf[InputFormDefinitionModel], data.get("id").getAsString)
        model.toJson
      case "view" =>
        val cachedTemplates = data.getAsJsonArray("cached").toSeq.map(_.getAsString)
        val model: LoadNodeViewModel = DependencyResolver.resolve(classOf[LoadNodeViewModel])
        model.initialize(data.getAsJsonPrimitive("newPath").getAsString, cachedTemplates, initializeUiElements = true, tryCreateResult = false)
        if (model.getNodeId.isDefined) {
          nodeId = model.getNodeId
        }
        model.toJson
      case "loginStage1" =>
        val model: LoginModel = DependencyResolver.resolve(classOf[LoginModel])
        model.handleStage1Request(data)
      case "loginStage2" =>
        val model: LoginModel = DependencyResolver.resolve(classOf[LoginModel])
        model.handleStage2Request(data)
      case "idle" => new JsonPrimitive("")
      case "logout" =>
        val model: LoginModel = DependencyResolver.resolve(classOf[LoginModel])
        model.handleLogout
      case "typeahead" =>
        val model: TypeaheadModel = DependencyResolver.resolve(classOf[TypeaheadModel])
        model.initialize(path, data.get("property").getAsString, data.get("searchText").getAsString)
        model.toJson
      case "register" =>
        val model: RegisterModel = DependencyResolver.resolve(classOf[RegisterModel])
        model.getRegisterObject
      case "searchText" =>
        val model: SearchTextModel = DependencyResolver.resolve(classOf[SearchTextModel])
        model.handle(data.get("text").getAsString).get
      case _ =>
        val model: ExecuteActionModel = DependencyResolver.resolve(classOf[ExecuteActionModel])
        model.handleRequest(data, path)
    }
  }

  private def handleUpdates(versionId: Option[Long], path: String, request: JsonObject, userChanged: Boolean, response: JsonObject) {

    if (userChanged) {
      this.changeTracker.invalidate()
    }
    val changesCache: ChangesCache = this.dataCache.get(classOf[ChangesCache])
    val newVersionId = changesCache.getLastChangeId
    if (newVersionId.isDefined && !(newVersionId == versionId)) {
      response.add("versionId", new JsonPrimitive(newVersionId.get))
    }
    if (userChanged) {
      val notificationsModel: UserInterfaceElementsModel = DependencyResolver.resolve(classOf[UserInterfaceElementsModel])
      notificationsModel.handle(response, newVersionId)
    }
    if ((newVersionId == versionId) && nodeId.isEmpty) {
      return
    }
    val model: LoadUpdatesModel = DependencyResolver.resolve(classOf[LoadUpdatesModel])
    model.handle(versionId, newVersionId, path, request, response, this.nodeId)
  }

  def handleRequest() {

    val requestContext: RequestContext = RequestContextHolder.getContext
    val requestData: JsonObject = getRequestData
    val path: JsonElement = requestData.get("path")
    if (path == null) {
      requestContext.sendError(404)
      return
    }
    val command: JsonElement = requestData.get("command")
    if (command == null) {
      requestContext.sendError(404)
      return
    }
    val response: JsonObject = new JsonObject
    val currentUser: CurrentUser = RequestContextHolder.getContext.getCurrentUser
    val userId: Option[Long] = currentUser.getUserId
    val userVerified: Boolean = isUserVerified
    val administrator: Boolean = currentUser.isAdministrator
    try {
      val resultData: JsonElement = handleDataRequest(requestData, command.getAsString, path.getAsString)
      if (resultData.isJsonObject) {
        val resultObject: JsonObject = resultData.getAsJsonObject
        if (resultObject.has("versionId")) {
          val versionId: JsonElement = resultObject.get("versionId")
          handleUpdates(if (versionId != null) Option[Long](versionId.getAsLong) else None, path.getAsString, requestData, userId != currentUser.getUserId, response)
        }
      }
      response.add("data", resultData)
      response.addProperty("success", true)
    }
    catch {
      case e: Exception =>
        LoggingHelper.logException(getClass, e)
        response.addProperty("success", false)
        response.addProperty("message", StringHelper.getMessage(e))
    }
    val userState: JsonObject = new JsonObject
    response.add("user", userState)
    val updatedUserId = currentUser.getUserId
    userState.addProperty("loggedIn", updatedUserId.isDefined)
    if (updatedUserId.isDefined) {
      userState.addProperty("userName", currentUser.getUserName.get)
    }
    if (userVerified != isUserVerified) {
      userState.addProperty("userNotVerified", userVerified)
    }
    if (administrator != currentUser.isAdministrator) {
      userState.addProperty("showConfiguration", currentUser.isAdministrator)
    }
    val output: OutputStream = requestContext.getOutputStream
    try {
      output.write(response.toString.getBytes)
    } finally {
      if (output != null) output.close()
    }
    requestContext.setContentType("application/json")
  }
}
