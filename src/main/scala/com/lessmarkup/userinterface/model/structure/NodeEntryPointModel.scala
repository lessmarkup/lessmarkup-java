/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

import java.io.{OutputStreamWriter, Writer}

import com.google.gson.{JsonArray, JsonNull, JsonObject}
import com.google.inject.Inject
import com.lessmarkup.Constants
import com.lessmarkup.dataobjects.Smile
import com.lessmarkup.engine.filesystem.TemplateContext
import com.lessmarkup.framework.helpers.{DependencyResolver, ImageHelper, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{ChangesCache, DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.recordmodel.RecordModelCache
import com.lessmarkup.interfaces.security.CurrentUser
import com.lessmarkup.interfaces.structure.{CachedNodeInformation, NodeCache}
import com.lessmarkup.interfaces.system._
import com.lessmarkup.userinterface.model.user.LoginModel
import com.samskivert.mustache.{Mustache, Template}

import scala.collection.JavaConversions

class NodeEntryPointModel @Inject() (dataCache: DataCache, domainModelProvider: DomainModelProvider) {

  class Context(dataCache: DataCache) extends TemplateContext(dataCache) {
    private var useScripts: Boolean = false
    private var title: String = null
    private var initialData: String = null
    private var serverConfiguration: String = null
    private var languages: String = null

    def getUseScripts: Boolean = {
      useScripts
    }

    def setUseScripts(use: Boolean) = {
      this.useScripts = use
    }

    def getTitle: String = {
      title
    }

    def setTitle(title: String) = {
      this.title = title
    }

    def getInitialData: String = {
      initialData
    }

    def setInitialData(data: String) = {
      this.initialData = data
    }

    def getServerConfiguration: String = {
      serverConfiguration
    }

    def setServerConfiguration(data: String): Unit = {
      this.serverConfiguration = data
    }

    def getLanguages: String = {
      this.languages
    }

    def setLanguages(languages: String) = {
      this.languages = languages
    }

    def getTopMenu: String = {
      val builder: StringBuilder = new StringBuilder
      val nodeCache: NodeCache = dataCache.get(classOf[NodeCache])
      nodeCache.getNodes.filter(n => n.addToMenu && n.enabled).foreach(menuNode => builder.append(String.format(
        "<li ng-style=\"{ active: path === '%s' }}\"><a href=\"%s\" ng-click=\"navigateToView('%s')\">%s</a></li>",
        menuNode.fullPath, menuNode.fullPath, menuNode.fullPath, menuNode.title)))
      builder.toString()
    }

    def getNoScriptBlock: String = {
      Constants.EngineNoScriptBlock
    }

    def getGoogleAnalytics: String = {
      val configuration: SiteConfiguration = this.dataCache.get(classOf[SiteConfiguration])
      val resourceId: String = configuration.googleAnalyticsResource
      if (StringHelper.isNullOrWhitespace(resourceId)) {
        return ""
      }
      String.format(this.dataCache.get(classOf[ResourceCache]).readText("/views/googleAnalytics.html").get, resourceId)
    }
  }

  private final val context: Context = new Context(dataCache)
  private var viewData: LoadNodeViewModel = null
  private var nodeLoadError: String = null
  private var versionId: Option[Long] = None

  private def checkBrowser(requestContext: RequestContext) {
  }

  private def prepareInitialData(requestContext: RequestContext, path: String) {
    val initialData: JsonObject = new JsonObject
    val changesCache: ChangesCache = dataCache.get(classOf[ChangesCache])
    versionId = changesCache.getLastChangeId
    val currentUser: CurrentUser = RequestContextHolder.getContext.getCurrentUser
    initialData.addProperty("loggedIn", currentUser.getUserId.isDefined)
    initialData.addProperty("userNotVerified", !currentUser.isApproved || !currentUser.emailConfirmed)
    if (currentUser.getUserName.isDefined) {
      initialData.addProperty("userName", currentUser.getUserName.get)
    }
    else {
      initialData.add("userName", JsonNull.INSTANCE)
    }
    initialData.addProperty("showConfiguration", currentUser.isAdministrator)
    if (versionId.isDefined) {
      initialData.addProperty("versionId", versionId.get)
    }
    else {
      initialData.add("versionId", JsonNull.INSTANCE)
    }
    initialData.add("loadedNode", this.viewData.toJson)
    initialData.addProperty("path", if (path != null) path else "")
    if (nodeLoadError != null) {
      initialData.addProperty("nodeLoadError", nodeLoadError)
    }
    context.setInitialData(initialData.toString)
  }

  private def prepareServerConfiguration(requestContext: RequestContext) {
    val serverConfiguration: JsonObject = new JsonObject
    val engineConfiguration: EngineConfiguration = requestContext.getEngineConfiguration
    val siteConfiguration: SiteConfiguration = this.dataCache.get(classOf[SiteConfiguration])
    var adminLoginPage: String = siteConfiguration.adminLoginPage
    if (adminLoginPage == null || adminLoginPage.length == 0) {
      adminLoginPage = RequestContextHolder.getContext.getEngineConfiguration.getAdminLoginPage
    }
    serverConfiguration.addProperty("hasLogin", siteConfiguration.hasUsers || adminLoginPage == null || adminLoginPage.length == 0)
    serverConfiguration.addProperty("hasSearch", siteConfiguration.hasSearch)
    serverConfiguration.addProperty("configurationPath", "/" + Constants.NodePathConfiguration)
    serverConfiguration.addProperty("rootPath", requestContext.getRootPath)
    serverConfiguration.addProperty("rootTitle", siteConfiguration.siteName)
    serverConfiguration.addProperty("profilePath", "/" + Constants.NodePathProfile)
    serverConfiguration.addProperty("forgotPasswordPath", "/" + Constants.NodePathForgotPassword)
    val notificationsModel: UserInterfaceElementsModel = DependencyResolver(classOf[UserInterfaceElementsModel])
    notificationsModel.handle(serverConfiguration, this.versionId)
    serverConfiguration.addProperty("recaptchaPublicKey", engineConfiguration.getRecaptchaPublicKey)
    serverConfiguration.addProperty("maximumFileSize", siteConfiguration.maximumFileSize)
    val domainModel: DomainModel = this.domainModelProvider.create
    try {
      val smiles: JsonArray = new JsonArray
      serverConfiguration.add("smiles", smiles)
      for (smile <- domainModel.query.from(classOf[Smile]).toList(classOf[Smile])) {
        val smileTarget: JsonObject = new JsonObject
        smileTarget.addProperty("id", smile.id)
        smileTarget.addProperty("code", smile.code)
        smiles.add(smileTarget)
      }
      serverConfiguration.addProperty("smilesBase", "/image/smile/")
    } finally {
      if (domainModel != null) domainModel.close()
    }
    serverConfiguration.addProperty("useGoogleAnalytics", siteConfiguration.googleAnalyticsResource != null)
    val recordModelCache: RecordModelCache = this.dataCache.get(classOf[RecordModelCache])
    serverConfiguration.addProperty("loginModelId", recordModelCache.getDefinition(classOf[LoginModel]).get.getId)
    serverConfiguration.addProperty("pageSize", engineConfiguration.getRecordsPerPage)
    context.setServerConfiguration(serverConfiguration.toString)
  }

  private def prepareLanguages() {
    val languages: JsonArray = new JsonArray
    val languageCache: LanguageCache = this.dataCache.get(classOf[LanguageCache])
    import scala.collection.JavaConversions._
    for (sourceLanguage <- JavaConversions.asJavaCollection(languageCache.getLanguages)) {
      val targetLanguage: JsonObject = new JsonObject
      targetLanguage.addProperty("selected", false)
      targetLanguage.addProperty("id", sourceLanguage.getShortName.toLowerCase)
      targetLanguage.addProperty("shortName", sourceLanguage.getShortName)
      targetLanguage.addProperty("name", sourceLanguage.getName)
      targetLanguage.addProperty("isDefault", sourceLanguage.getIsDefault)
      targetLanguage.addProperty("iconUrl", if (sourceLanguage.getIconId.isDefined) ImageHelper.getImageUrl(sourceLanguage.getIconId.get) else "")
      val translations: JsonObject = new JsonObject
      import scala.collection.JavaConversions._
      for (translation <- JavaConversions.asJavaIterable(sourceLanguage.getTranslations.toIterable)) {
        translations.addProperty(translation._1, translation._2)
      }
      targetLanguage.add("translations", translations)
      languages.add(targetLanguage)
    }
    context.setLanguages(languages.toString)
  }

  def initialize(path: String): Boolean = {
    val requestContext: RequestContext = RequestContextHolder.getContext
    checkBrowser(requestContext)
    if (context.getUseScripts) {
      val cookie = requestContext.getCookie("noscript")
      if (cookie.isDefined && ("true" == cookie.get.getValue)) {
        context.setUseScripts(false)
      }
    }
    val queryString: String = requestContext.getPath
    if (queryString != null && (queryString.endsWith("?noscript") || ("noscript" == queryString))) {
      context.setUseScripts(false)
    }
    this.nodeLoadError = null
    viewData = DependencyResolver(classOf[LoadNodeViewModel])
    try {
      if (!viewData.initialize(path, null, initializeUiElements = true, tryCreateResult = true)) {
        return false
      }
    }
    catch {
      case e: Exception =>
        this.nodeLoadError = e.getMessage
        if (this.nodeLoadError == null) {
          this.nodeLoadError = e.toString
        }
    }
    val nodeCache: NodeCache = this.dataCache.get(classOf[NodeCache])
    val rootNode: CachedNodeInformation = nodeCache.getRootNode.get
    context.setTitle(rootNode.title)
    prepareInitialData(requestContext, path)
    prepareServerConfiguration(requestContext)
    prepareLanguages()

    !context.getUseScripts
  }

  def handleRequest() {
    val requestContext: RequestContext = RequestContextHolder.getContext
    val resourceCache: ResourceCache = dataCache.get(classOf[ResourceCache])
    val compiler: Mustache.Compiler = TemplateContext.createCompiler(resourceCache)
    val template: Template = compiler.compile(resourceCache.readText("views/entrypoint.html").get)
    val html: String = template.execute(context)
    val writer: Writer = new OutputStreamWriter(requestContext.getOutputStream)
    try {
      writer.write(html)
    } finally {
      if (writer != null) writer.close()
    }
  }
}