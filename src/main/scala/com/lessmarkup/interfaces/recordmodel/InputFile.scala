/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.recordmodel

class InputFile {
  private var `type`: String = null
  private var file: Array[Byte] = null
  private var name: String = null

  def getType: String = `type`

  def setType(`type`: String) {
    this.`type` = `type`
  }

  def getFile: Array[Byte] = file

  def setFile(file: Array[Byte]) {
    this.file = file
  }

  def getName: String = name

  def setName(name: String) {
    this.name = name
  }
}