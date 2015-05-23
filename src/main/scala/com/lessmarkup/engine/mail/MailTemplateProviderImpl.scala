/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.mail

import com.google.inject.Inject
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.module.Implements
import com.lessmarkup.interfaces.system.{MailTemplateModel, MailTemplateProvider, ResourceCache}
import com.samskivert.mustache.{Mustache, Template}

@Implements(classOf[MailTemplateProvider])
class MailTemplateProviderImpl @Inject() (dataCache: DataCache) extends MailTemplateProvider {

  def executeTemplate[T <: MailTemplateModel](`type`: Class[T], viewPath: String, model: T): String = {
    val resourceCache: ResourceCache = dataCache.get(classOf[ResourceCache])
    val compiler: Mustache.Compiler = Mustache.compiler
    val text = resourceCache.readText(viewPath)
    if (text.isEmpty) {
      throw new IllegalArgumentException
    }
    val template: Template = compiler.compile(text.get)
    template.execute(model)
  }
}