/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data.dialects

import java.util.OptionalInt

trait DatabaseLanguageDialect {
  def getTypeDeclaration(dataType: DatabaseDataType, sizeLimit: OptionalInt, required: Boolean, characterSet: String): String

  def getTypeDeclaration(dataType: DatabaseDataType, required: Boolean): String = {
    getTypeDeclaration(dataType, OptionalInt.empty, required, null)
  }

  def decorateName(name: String): String

  def getDataType(dataType: String): DatabaseDataType

  def paging(from: Int, count: Int): String
}