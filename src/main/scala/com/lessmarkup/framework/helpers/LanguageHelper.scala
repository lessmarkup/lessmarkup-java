/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.helpers

import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.system.LanguageCache

object LanguageHelper {
  private def getLanguageCache: LanguageCache = {
    DependencyResolver(classOf[DataCache]).get(classOf[LanguageCache])
  }

  private final def NoTranslationMessage = "No Translation"

  private def getTranslation(moduleId: String, textId: String): Option[String] = {
    getLanguageCache.getTranslation(textId, Option(moduleId))
  }

  def getText(moduleType: String, id: String, args: Any*): String = {
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
