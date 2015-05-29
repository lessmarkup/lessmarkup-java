/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.system

import com.lessmarkup.interfaces.cache.CacheHandler

trait SiteConfiguration extends CacheHandler {
  def siteName: String
  def recordsPerPage: Int
  def noReplyEmail: String
  def noReplyName: String
  def defaultUserGroup: String
  def maximumFileSize: Int
  def thumbnailWidth: Int
  def thumbnailHeight: Int
  def maximumImageWidth: Int
  def hasUsers: Boolean
  def hasNavigationBar: Boolean
  def hasSearch: Boolean
  def hasLanguages: Boolean
  def hasCurrencies: Boolean
  def adminLoginPage: String
  def adminNotifyNewUsers: Boolean
  def adminApprovesNewUsers: Boolean
  def userAgreement: String
  def googleAnalyticsResource: String
  def validFileType: String
  def validFileExtension: String
  def engineOverride: String
  def getProperty(key: String): String
}