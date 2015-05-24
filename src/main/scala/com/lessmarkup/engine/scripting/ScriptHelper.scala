/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.scripting

import com.google.gson.JsonElement

object ScriptHelper {
  def evaluateExpression(expression: String, objectToEvaluate: JsonElement): Boolean = {
    val parser: ExpressionParser = new ExpressionParser(expression)
    parser.evaluate(objectToEvaluate)
  }
}