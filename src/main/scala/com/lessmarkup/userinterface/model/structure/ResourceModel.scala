/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

import java.nio.charset.StandardCharsets

import com.google.inject.Inject
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.system.{RequestContext, ResourceCache}

class ResourceModel @Inject() (dataCache: DataCache) {

  private var contentType: String = null
  private var path: String = null
  private var extension: String = null

  def initialize(path: String): Boolean = {
    if (path == null || path.length == 0) {
      return false
    }
    val resourceCache: ResourceCache = dataCache.get(classOf[ResourceCache])
    if (!resourceCache.resourceExists(path)) {
      return false
    }
    val lastDotPoint: Int = path.lastIndexOf('.')
    if (lastDotPoint <= 0) {
      return false
    }
    this.extension = path.substring(lastDotPoint + 1).toLowerCase
    extension match {
      case "html" =>
        this.contentType = "text/html"
      case "txt" =>
        this.contentType = "text/plain"
      case "js" =>
        this.contentType = "text/javascript"
      case "css" =>
        this.contentType = "text/css"
      case "jpeg" =>
        this.contentType = "image/jpeg"
      case "jpg" =>
        this.contentType = "image/jpeg"
      case "gif" =>
        this.contentType = "image/gif"
      case "png" =>
        this.contentType = "image/png"
      case "eot" =>
        this.contentType = "application/vnd.ms-fontobject"
      case "otf" =>
        this.contentType = "application/x-font-opentype"
      case "svg" =>
        this.contentType = "image/svg+xml"
      case "ttf" =>
        this.contentType = "application/font-sfnt"
      case "woff" =>
        this.contentType = "application/font-woff"
      case "woff2" =>
        this.contentType = "application/font-woff"
      case "map" =>
        this.contentType = "text/map"
      case "ts" =>
        this.contentType = "text/typescript"
      case _ =>
        return false
    }

    this.path = path

    true
  }

  def handleRequest() {
    val requestContext: RequestContext = RequestContextHolder.getContext
    requestContext.addHeader("Cache-Control", "public, max-age=3600")
    requestContext.addHeader("Content-Type", this.contentType)
    val resourceCache: ResourceCache = this.dataCache.get(classOf[ResourceCache])
    var resourceBytes: Array[Byte] = null
    this.extension match {
      case "html" =>
        resourceBytes = resourceCache.parseText(this.path).get.getBytes(StandardCharsets.UTF_8)
      case _ =>
        resourceBytes = resourceCache.readBytes(this.path).get
    }
    requestContext.getOutputStream.write(resourceBytes, 0, resourceBytes.length)
  }
}