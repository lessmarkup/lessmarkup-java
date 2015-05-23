package com.lessmarkup.interfaces.system

import com.lessmarkup.interfaces.cache.CacheHandler

trait LanguageCache extends CacheHandler {
  def getCurrentLanguageId: Option[String]

  def getTranslation(id: String, moduleType: Option[String], throwIfNotFound: Boolean): Option[String] = {
    getTranslation(getCurrentLanguageId, id, moduleType, throwIfNotFound)
  }

  def getTranslation(id: String, moduleType: Option[String]): Option[String] = {
    getTranslation(id, moduleType, throwIfNotFound = true)
  }

  def getTranslation(languageId: Option[String], id: String, moduleType: Option[String], throwIfNotFound: Boolean): Option[String]

  def getTranslation(languageId: Option[String], id: String, moduleType: Option[String]): Option[String] = {
    getTranslation(languageId, id, moduleType, throwIfNotFound = true)
  }

  def getLanguages: List[Language]
}