/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.filesystem

import com.lessmarkup.interfaces.module.ModuleConfiguration
import com.samskivert.mustache.Template

class ResourceReference(
  var path: String,
  var module: Option[ModuleConfiguration] = None,
  var recordId: Long = 0,
  var binary: Option[Array[Byte]] = None,
  var minified: Boolean = false,
  var extension: Option[String] = None,
  var template: Option[Template] = None
)