/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.language

import java.util.OptionalLong

import com.google.inject.Inject
import com.lessmarkup.Constants
import com.lessmarkup.framework.helpers.{LanguageHelper, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.AbstractCacheHandler
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.module.{Implements, ModuleProvider}
import com.lessmarkup.interfaces.system.{Language, LanguageCache}
import com.thoughtworks.xstream.XStream

import scala.collection.JavaConversions._

object LanguageCacheImpl {

  private def readLanguageFile(xml: String): XmlLanguageFile = {
    val xstream: XStream = new XStream
    xstream.alias("file", classOf[XmlLanguageFile])
    xstream.alias("translation", classOf[XmlTranslation])
    xstream.useAttributeFor(classOf[XmlLanguageFile], "name")
    xstream.useAttributeFor(classOf[XmlLanguageFile], "id")
    xstream.useAttributeFor(classOf[XmlLanguageFile], "shortName")
    xstream.useAttributeFor(classOf[XmlTranslation], "id")
    xstream.useAttributeFor(classOf[XmlTranslation], "text")

    xstream.fromXML(xml).asInstanceOf[XmlLanguageFile]
  }
}

@Implements(classOf[LanguageCache])
class LanguageCacheImpl @Inject() (domainModelProvider: DomainModelProvider, moduleProvider: ModuleProvider)
  extends AbstractCacheHandler(Array[Class[_]](classOf[Language]))
  with LanguageCache {

  private val languagesMap: Map[String, CachedLanguage] = loadLanguages

  private val defaultLanguageId: Option[String] = {
    val defaultLanguage: Option[CachedLanguage] = languagesMap.values.find(v => v.getIsDefault)
    val defaultOrFirstLanguage = if (defaultLanguage.isDefined) defaultLanguage else languagesMap.values.headOption
    if (defaultOrFirstLanguage.isDefined) Option(defaultOrFirstLanguage.get.getShortName) else None
  }

  private def loadLanguageFile(bytes: Array[Byte], moduleType: String): Option[CachedLanguage] = {

    val xml: String = StringHelper.binaryToString(bytes)
    val file: XmlLanguageFile = LanguageCacheImpl.readLanguageFile(xml)

    if (file == null || file.getTranslations.isEmpty) {
      return None
    }

    val translations: Map[String, String] = file.getTranslations
      .filter(t => t.getId.length > 0)
      .map(t => (LanguageHelper.getFullTextId(moduleType, t.getId), t.getText))
      .toMap

    Option(new CachedLanguage(
      name = file.getName,
      shortName = file.getShortName.toLowerCase,
      translations = translations
    ))
  }

  def initialize(objectId: OptionalLong) {
  }

  def loadLanguages: Map[String, CachedLanguage] = {
    val languageFiles = moduleProvider
      .getModules
      .flatten(m => m.getElements.map(e => (m, e)))
      .filter(m => m._2.endsWith(".language.xml"))
      .map(m => loadLanguageFile(m._1.getResourceAsBytes(m._2), m._1.getModuleType))
      .filter(_.isDefined)
      .map(l => (l.get.getShortName.toLowerCase, l.get))
      .toMap

    val domainModel: DomainModel = domainModelProvider.create
    try {
      val databaseFiles = domainModel.query
          .from(classOf[com.lessmarkup.dataobjects.Language])
          .where("visible = $", true)
          .toList(classOf[com.lessmarkup.dataobjects.Language])
          .map(lang => {

        val translations = domainModel.query
          .from(classOf[com.lessmarkup.dataobjects.Translation])
          .where("LanguageId = $", lang.getId)
          .toList(classOf[com.lessmarkup.dataobjects.Translation])
          .map(t => (t.getKey, t.getText))
          .toMap

        val shortName: String = lang.getShortName.toLowerCase
        val existingLanguage = languageFiles.get(shortName)

        if (existingLanguage.isDefined) {
          new CachedLanguage(
            name = lang.getName,
            shortName = lang.getShortName,
            translations = translations ++ existingLanguage.get.getTranslations
          )
        } else {
          new CachedLanguage(
            name = lang.getName,
            shortName = lang.getShortName,
            translations = translations,
            id = Option(lang.getId)
          )
        }
      }).map(l => (l.getShortName.toLowerCase, l))

      languageFiles.filter(l => !databaseFiles.contains(l._1)) ++ databaseFiles

    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def getCurrentLanguageId: Option[String] = {
    val languageId: String = RequestContextHolder.getContext.getLanguageId
    if (languageId == null) {
      return defaultLanguageId
    }
    val language: Option[CachedLanguage] = languagesMap.get(languageId)
    if (language.isEmpty) {
      return defaultLanguageId
    }

    Option(languageId)
  }

  def getTranslation(languageId: Option[String], id: String, moduleType: Option[String], throwIfNotFound: Boolean): Option[String] = {

    val checkedLanguageId = if (languageId.isDefined) languageId else defaultLanguageId

    val language: Option[CachedLanguage] = if (checkedLanguageId.isDefined) languagesMap.get(checkedLanguageId.get) else None

    internalGetTranslation(language, id, moduleType, throwIfNotFound)
  }

  def getLanguages: List[Language] = {
    languagesMap.values.toList
  }

  private def internalGetTranslation(language: Option[CachedLanguage], id: String, moduleType: Option[String], throwIfNotFound: Boolean): Option[String] = {

    val checkedId = if (moduleType.isDefined) LanguageHelper.getFullTextId(moduleType.get, id) else id

    if (language.isDefined) {
      return language.get.getTranslations.get(checkedId)
    }

    if (!throwIfNotFound) {
      return None
    }

    if (Constants.IsDebug) {
      Option("$$-" + id)
    }
    else {
      throw new IllegalArgumentException
    }
  }
}