/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.filesystem

import java.io.{Reader, StringReader, Writer}

import com.lessmarkup.Constants
import com.lessmarkup.framework.helpers.{LanguageHelper, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.system.{ResourceCache, SiteConfiguration}
import com.samskivert.mustache.Mustache.{Lambda, TemplateLoader}
import com.samskivert.mustache.{Mustache, Template}

object TemplateContext {
  def createCompiler(resourceCache: ResourceCache): Mustache.Compiler = {
    Mustache.compiler.escapeHTML(false).withDelims("[[ ]]").withLoader(
      new TemplateLoader {
        override def getTemplate(s: String): Reader = {
          new StringReader(resourceCache.readText(s).getOrElse(""))
        }
      })
  }
}

class TemplateContext(dataCache: DataCache) {

  private final val text: Mustache.Lambda = new Lambda {
    override def execute(fragment: Template#Fragment, writer: Writer): Unit = {
      val key = fragment.execute()
      val text = LanguageHelper.getText(Constants.ModuleTypeMain, key)
      writer.write(text)
    }
  }

  private final val property: Mustache.Lambda = new Lambda {
    override def execute(fragment: Template#Fragment, writer: Writer): Unit = {
      val key = StringHelper.toJsonCase(fragment.execute())
      val value = key match {
        case "rootPath" => RequestContextHolder.getContext.getRootPath
        case _ => dataCache.get(classOf[SiteConfiguration]).getProperty(key)
      }
      writer.write(value)
    }
  }

  private final val map: Mustache.Lambda = new Lambda {
    override def execute(fragment: Template#Fragment, writer: Writer): Unit = {
      val key = fragment.execute()
      val value = RequestContextHolder.getContext.mapPath(key)
      writer.write(value)
    }
  }

  def getText: Mustache.Lambda = text
  def getProperty: Mustache.Lambda = property
  def getMap: Mustache.Lambda = map
}