/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.interfaces.data

object BinaryData {
  def apply(data: Seq[Byte]): BinaryData = {
    new BinaryData(data)
  }
}

class BinaryData(val data: Seq[Byte]) {
  def length = data.length
}
