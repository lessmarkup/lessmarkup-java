package com.lessmarkup.framework.helpers

import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.system.LanguageCache

object LanguageHelper {
  private def getLanguageCache: LanguageCache = {
    DependencyResolver.resolve(classOf[DataCache]).get(classOf[LanguageCache])
  }

  private final def NoTranslationMessage = "No Translation"

  private def getTranslation(moduleId: String, textId: String): Option[String] = {
    getLanguageCache.getTranslation(textId, Option(moduleId))
  }

  @Deprecated
  def getText(moduleType: String, id: String): String = {
    internalGetText(moduleType, id)
  }

  @Deprecated
  def getText(moduleType: String, id: String, param1: Any) = {
    internalGetText(moduleType, id, param1)
  }

  @Deprecated
  def getText(moduleType: String, id: String, param1: Any, param2: Any) = {
    internalGetText(moduleType, id, param1, param2)
  }

  private def internalGetText(moduleType: String, id: String, args: Any*): String = {
    if (id == null) {
      return NoTranslationMessage
    }

    val mask = "(.+)\\.(.+)".r

    val translation = id match {
      case mask(moduleId, textId) => getTranslation(moduleId, textId)
      case _ => getTranslation(moduleType, id)
    }

    if (translation.isDefined && args.nonEmpty) {
      translation.get.format(args)
    } else {
      translation.getOrElse(NoTranslationMessage)
    }
  }

  def getFullTextId(moduleType: String, id: String): String = {
    s"$moduleType.$id"
  }
}