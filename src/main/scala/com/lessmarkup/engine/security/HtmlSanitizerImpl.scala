/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.security

import com.lessmarkup.interfaces.security.HtmlSanitizer
import org.owasp.html.{HtmlPolicyBuilder, PolicyFactory, Sanitizers}

class HtmlSanitizerImpl extends HtmlSanitizer {
  def sanitize(html: String, tagsToRemove: Array[String]): String = {
    var sanitizer: PolicyFactory = Sanitizers.FORMATTING
    val builder = new HtmlPolicyBuilder()
    tagsToRemove.foreach(t => builder.disallowElements(t))
    sanitizer = sanitizer.and(builder.toFactory)
    sanitizer.sanitize(html)
  }
}