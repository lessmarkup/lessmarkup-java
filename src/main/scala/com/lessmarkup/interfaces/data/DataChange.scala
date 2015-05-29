/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

import java.time.OffsetDateTime

import com.lessmarkup.interfaces.cache.EntityChangeType

trait DataChange {
  def getId: Long
  def getEntityId: Long
  def getCreated: OffsetDateTime
  def getUserId: Option[Long]
  def getParameter1: Long
  def getParameter2: Long
  def getParameter3: Long
  def getType: EntityChangeType
}