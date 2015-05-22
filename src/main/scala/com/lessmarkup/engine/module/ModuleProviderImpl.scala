package com.lessmarkup.engine.module

import java.io._
import java.lang.reflect.Modifier
import java.net.{MalformedURLException, URL, URLClassLoader, URLDecoder}
import java.util.zip.ZipInputStream

import com.lessmarkup.dataobjects.Module
import com.lessmarkup.framework.helpers.{DependencyResolver, LoggingHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.exceptions.CommonException
import com.lessmarkup.interfaces.module.{Implements, ModuleConfiguration, ModuleInitializer, ModuleProvider}
import com.lessmarkup.interfaces.structure.{Tuple, NodeHandler}

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import scala.util.Try

@Implements(classOf[ModuleProvider])
class ModuleProviderImpl extends ModuleProvider {

  private val modules = discoverAndRegisterModules()
  private val nodeHandlers: Map[String, (Class[_ <: NodeHandler], String)] = modules
    .flatMap(m => m.getInitializer.getNodeHandlerTypes.map(h => (m, h)))
    .map(h => (h._2.getSimpleName, (h._2, h._1.getModuleType))).toMap

  def getModules: java.util.Collection[ModuleConfiguration] = modules

  def discoverAndRegisterModules(): List[ModuleConfiguration] = {
    val classLoader: URLClassLoader = getClass.getClassLoader.asInstanceOf[URLClassLoader]

    val systemModules = (for (url <- classLoader.getURLs if url.getProtocol == "file" if !url.getPath.endsWith(".jar"))
        yield discoverModules(url, isSystem = true, Option(classLoader))
      ).flatten.toList

    val modulesPath: String = RequestContextHolder.getContext.getEngineConfiguration.getModulesPath
    if (modulesPath == null || modulesPath.length == 0) {
      return systemModules
    }

    val modulesDirectory: File = new File(modulesPath)
    if (!modulesDirectory.exists) {
      return systemModules
    }

    systemModules ::: (for (file <- modulesDirectory.listFiles if file.getName.endsWith(".jar"))
      yield discoverModules(file.toURI.toURL, isSystem = false, null)).flatten.toList
  }

  private def directoryEntryPath(basePath: String, node: File) = {
    val fullPath = node.toURI.toURL.getPath
    if (fullPath.startsWith(basePath))
      fullPath.substring(basePath.length)
    else
      fullPath
  }

  private def directoryGetFiles(node: File): Array[File] = {
    val files = node.listFiles
    if (files == null) {
      val userName: String = System.getProperty("user.name")
      val writer: PrintWriter = new PrintWriter(System.out)

      try {
        writer.write(userName)
      } finally {
        if (writer != null) writer.close()
      }

      throw new CommonException("Access denied for user: " + userName + " and file:" + node.getPath)
    }
    files
  }

  private def flattenDirectoryTree(basePath: String, nodes: Array[File]) : List[String] =
    if (nodes.head.isDirectory) flattenDirectoryTree(basePath, directoryGetFiles(nodes.head)) ::: flattenDirectoryTree(basePath, nodes.tail)
    else directoryEntryPath(basePath, nodes.head) :: flattenDirectoryTree(basePath, nodes.tail)

  private def listAllFileSystemModuleElements(basePath: String, directory: File) : List[String] = {
    flattenDirectoryTree(basePath, directoryGetFiles(directory))
  }

  private def listAllFileSystemModuleElements(path: String): List[String] = {
    val moduleDirectory: File = new File(path)
    try {
      listAllFileSystemModuleElements(moduleDirectory.toURI.toURL.getPath, moduleDirectory)
    }
    catch {
      case ex:
        MalformedURLException => LoggingHelper.logException(getClass, ex)
        List[String]()
    }
  }

  private def listAllJarModuleElements(path: String) = {
    val stream: InputStream = new FileInputStream(path)
    val zip: ZipInputStream = new ZipInputStream(stream)
    try {
      Stream.continually(zip.getNextEntry).takeWhile(_ != null).map(_.getName).toList
    }
    catch {
      case ex: IOException =>
        LoggingHelper.logException(getClass, ex)
        List[String]()
    } finally {
      if (stream != null)
        stream.close()
      if (zip != null)
        zip.close()
    }
  }

  private def listAllModuleElements(path: String) = {
    if (path.endsWith(".jar")) {
      listAllJarModuleElements(path)
    } else {
      listAllFileSystemModuleElements(path)
    }
  }

  private def constructModule(moduleUrl: URL, isSystem: Boolean, moduleInitializerClass: Class[_], classLoader: ClassLoader, elements: List[String]): Option[ModuleConfiguration] = {

    if (moduleInitializerClass.isInterface || Modifier.isAbstract(moduleInitializerClass.getModifiers) || !classOf[ModuleInitializer].isAssignableFrom(moduleInitializerClass)) {
      return None
    }

    val moduleInitializer = DependencyResolver.resolve(moduleInitializerClass).asInstanceOf[ModuleInitializer]

    if (moduleInitializer == null) {
      return None
    }

    moduleInitializer.initialize()

    val moduleConfiguration = new ModuleConfigurationImpl(moduleUrl, isSystem, moduleInitializer.getModuleType, elements.asJava, classLoader, moduleInitializer)

    Option(moduleConfiguration)
  }

  private def discoverModules(moduleUrl: URL, isSystem: Boolean, classLoader: Option[ClassLoader]): List[ModuleConfiguration] = {

    if (!(moduleUrl.getProtocol == "file")) {
      return List[ModuleConfiguration]()
    }

    val elements = Try ({ listAllModuleElements(URLDecoder.decode(moduleUrl.getPath, "UTF-8")) })
      .recoverWith { case ex: Throwable => ex.printStackTrace(); return List[ModuleConfiguration]() }
      .getOrElse(List[String]())

    val initializedClassLoader = classLoader.getOrElse(URLClassLoader.newInstance(Array[URL](moduleUrl), getClass.getClassLoader))

    val classes: List[Class[_]] =
      elements
        .filter(_.endsWith(".class"))
        .map(path => path.substring(0, path.length - ".class".length).replaceAll("/", "."))
        .map(Class.forName(_, true, initializedClassLoader))

    val moduleInitializerClasses = classes.filter(classOf[ModuleInitializer].isAssignableFrom)

    DependencyResolver.loadClasses(classes)

    if (moduleInitializerClasses.isEmpty) {
      LoggingHelper.getLogger(getClass).info(String.format("Cannot find initalizer for module %s", moduleUrl))
      return List[ModuleConfiguration]()
    }

    moduleInitializerClasses
      .map(constructModule(moduleUrl, isSystem, _, initializedClassLoader, elements))
      .filter(_.isDefined)
      .map(_.get)
  }

  def updateModuleDatabase(domainModelProvider: DomainModelProvider) {

    if (domainModelProvider == null) {
      return
    }

    val domainModel: DomainModel = domainModelProvider.create
    try {

      val query = domainModel.query.from(classOf[Module]).where("removed = $", new java.lang.Boolean(false))
      val databaseModules = query.toList(classOf[Module]).toList
      val existingModules = modules.map(m => m.getUrl.toString -> m).toMap

      databaseModules.filter(m => !existingModules.contains(m.getPath)).foreach(m => {
        m.setRemoved(true)
        domainModel.update(m)
      })

      val existingDatabaseModules = databaseModules.filter(m => existingModules.contains(m.getPath))

      existingDatabaseModules.foreach(m => {
        val existingModule = existingModules.get(m.getPath).get
        m.setSystem(existingModule.isSystem)
        m.setModuleType(existingModule.getModuleType)
        domainModel.update(m)
      })

      val existingDatabasePaths = databaseModules.map(_.getPath).toSet

      modules.filter(m => !existingDatabasePaths.contains(m.getUrl.toString)).foreach(m => {
        val module: Module = new Module()
        module.setEnabled(true)
        module.setName(m.getInitializer.getName)
        module.setPath(m.getUrl.toString)
        module.setRemoved(false)
        module.setSystem(m.isSystem)
        module.setModuleType(m.getModuleType)
        domainModel.create(module)
      })

    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def getNodeHandlers: java.util.Collection[String] = nodeHandlers.keySet

  def getNodeHandler(id: String): Tuple[Class[_ <: NodeHandler], String] = {
    val ret = nodeHandlers.get(id).get
    new Tuple(ret._1, ret._2)
  }
}