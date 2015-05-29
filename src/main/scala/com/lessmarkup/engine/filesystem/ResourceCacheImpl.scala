/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.filesystem

import java.io.IOException
import java.util.logging.Level

import com.google.inject.Inject
import com.lessmarkup.dataobjects.SiteCustomization
import com.lessmarkup.framework.helpers.{LoggingHelper, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.Implements
import com.lessmarkup.interfaces.cache.{AbstractCacheHandler, DataCache}
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.module.{ModuleConfiguration, ModuleProvider}
import com.lessmarkup.interfaces.system.ResourceCache
import com.samskivert.mustache.{Mustache, Template}

import scala.collection.JavaConversions._

object ResourceCacheImpl {
  private def isExtensionSupported(extension: String): Boolean = {
    extension match {
      case "jpg" => true
      case "gif" => true
      case "png" => true
      case "html" => true
      case "xml" => true
      case "js" => true
      case "css" => true
      case "eot" => true
      case "svg" => true
      case "ttf" => true
      case "woff" => true
      case "woff2" => true
      case "map" => true
      case "ts" => true
      case _ => false
    }
  }

  private def extractExtension(path: String): Option[String] = {
    val pos: Int = path.lastIndexOf('.')
    if (pos <= 0) {
      None
    } else {
      Option(path.substring(pos + 1).toLowerCase)
    }
  }
}

@Implements(classOf[ResourceCache])
class ResourceCacheImpl @Inject() (moduleProvider: ModuleProvider, domainModelProvider: DomainModelProvider, dataCache: DataCache) extends AbstractCacheHandler(null) with ResourceCache {
  private val resources = loadDatabaseResources

  private val compiler: Mustache.Compiler = TemplateContext.createCompiler(this)

  private def loadModulesResources: Map[String, ResourceReference] = {

    moduleProvider.getModules
      .flatten(m => m.getElements.map(e => (m, e, ResourceCacheImpl.extractExtension(e))))
      .filter(e => e._3.isDefined && ResourceCacheImpl.isExtensionSupported(e._3.get))
      .map(e => (e._2, createResource(e._1, e._2, e._3.get))).toMap
  }

  private def loadDatabaseResources: Map[String, ResourceReference] = {

    val modulesResources = loadModulesResources

    if (RequestContextHolder.getContext.getEngineConfiguration.isCustomizationsDisabled) {
      return modulesResources
    }

    val domainModel: DomainModel = domainModelProvider.create
    try {

      domainModel.query.from(classOf[SiteCustomization]).toList(classOf[SiteCustomization]).map(record => {
        val recordPath: String = record.path
        val reference: Option[ResourceReference] = if (record.append) modulesResources.get(recordPath) else None
        if (record.append && reference.isDefined) {
          val binaryLength = if (reference.get.binary.isDefined) reference.get.binary.get.length else 0
          val binary: Array[Byte] = new Array[Byte](binaryLength + record.body.length)
          if (reference.get.binary.isDefined) {
            System.arraycopy(reference.get.binary.get, 0, binary, 0, binaryLength)
          }
          System.arraycopy(record.body, 0, binary, binaryLength, record.body.length)
          reference.get.binary = Option(binary)
          None
        } else {
          Option(new ResourceReference(
            path = record.path,
            recordId = record.id,
            binary = Option(record.body)
          ))
        }
      }).filter(_.isDefined)
        .map(r => (r.get.path, r.get))
        .toMap ++ modulesResources

    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  private def createResource(module: ModuleConfiguration, path: String, extension: String): ResourceReference = {
    new ResourceReference(
      module = Option(module),
      path = path,
      extension = Option(extension))
  }

  def resourceExists(path: String): Boolean = {
    resources.containsKey(path)
  }

  private def loadResource(path: String): Option[ResourceReference] = {
    val resourceOption: Option[ResourceReference] = resources.get(path)
    if (resourceOption.isEmpty) {
      return resourceOption
    }

    val resource = resourceOption.get

    if (resource.binary.isDefined || resource.module.isEmpty) {
      return resourceOption
    }

    resource synchronized {
      try {
        resource.binary = Option(resource.module.get.getResourceAsBytes(path))
      }
      catch {
        case e: IOException =>
          LoggingHelper.getLogger(getClass).log(Level.SEVERE, null, e)
      }
      if (resource.binary.isDefined) {
        if (resource.extension.isDefined && "html" == resource.extension.get) {
          val body: String = StringHelper.binaryToString(resource.binary.get)
          if (body.contains("[[")) {
            val template: Template = compiler.compile(body)
            resource.template = Option(template)
          }
        }
      }
    }

    resourceOption
  }

  def readBytes(path: String): Option[Array[Byte]] = {
    val resource: Option[ResourceReference] = loadResource(path)
    if (resource.isEmpty || resource.get.binary.isEmpty) {
      None
    } else {
      resource.get.binary
    }
  }

  def readText(path: String): Option[String] = {
    val resource = loadResource(path)
    if (resource.isEmpty || resource.get.binary.isEmpty) {
      None
    } else {
      Option(StringHelper.binaryToString(resource.get.binary.get))
    }
  }

  private def parseText(reference: ResourceReference): String = {
    if (reference.template.isEmpty) {
      StringHelper.binaryToString(reference.binary.get)
    } else {
      val context: TemplateContext = new TemplateContext(dataCache)
      reference.template.get.execute(context)
    }
  }

  def parseText(path: String): Option[String] = {
    val reference = loadResource(path)
    if (reference.isEmpty || reference.get.binary.isEmpty) {
      None
    } else {
      Option(parseText(reference.get))
    }
  }
}
