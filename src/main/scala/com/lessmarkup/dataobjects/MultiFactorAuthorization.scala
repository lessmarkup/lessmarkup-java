/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.dataobjects

object MultiFactorAuthorization {

  def apply(value: Int) = new MultiFactorAuthorization(value)

  final val NONE = MultiFactorAuthorization(0)
  final val ALWAYS = MultiFactorAuthorization(1)
  final val CHANGED_IP = MultiFactorAuthorization(2)
}

final class MultiFactorAuthorization(value: Int)
