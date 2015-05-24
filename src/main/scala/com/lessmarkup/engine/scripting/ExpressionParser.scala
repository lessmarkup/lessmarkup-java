/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.scripting

import com.google.gson.{JsonElement, JsonPrimitive}
import scala.collection.mutable.ListBuffer

class ExpressionParser(expression: String) {
  private final val atoms: List[ExpressionAtom] = parse(expression)

  def parse(expression: String): List[ExpressionAtom] = {

    val list = ListBuffer[ExpressionAtom]()

    var i: Int = 0
    while (i < expression.length) {
      val c: Char = expression.charAt(i)
      if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
        i += 1
      }
      else if (c == '=' && i + 1 < expression.length && expression.charAt(i + 1) == '=') {
        i += 2
        list += new ExpressionAtom(AtomType.EQUAL)
      }
      else if (c == '!' && i + 1 < expression.length && expression.charAt(i + 1) == '=') {
        i += 2
        list += new ExpressionAtom(AtomType.NOT_EQUAL)
      }
      else if (c == '!') {
        i += 1
        list += new ExpressionAtom(AtomType.INVERSE)
      }
      else if ((c == '\'' || c == '"') && expression.indexOf(c, i + 1) > i) {
        val next: Int = expression.indexOf(c, i + 1)
        list += new ExpressionAtom(AtomType.OBJECT, Option(new JsonPrimitive(expression.substring(i + 1, next))))
        i = next + 1
      }
      else if (c == '(') {
        list += new ExpressionAtom(AtomType.OPEN)
        i += 1
      }
      else if (c == ')') {
        list += new ExpressionAtom(AtomType.CLOSE)
        i += 1
      }
      else if (c == '&' && i + 1 < expression.length && expression.charAt(i + 1) == '&') {
        list += new ExpressionAtom(AtomType.AND)
        i += 2
      }
      else if (c == '|' && i + 1 < expression.length && expression.charAt(i + 1) == '|') {
        list += new ExpressionAtom(AtomType.OR)
        i += 2
      }
      else if (c == '+') {
        list += new ExpressionAtom(AtomType.PLUS)
        i += 1
      }
      else if (c == '-') {
        list += new ExpressionAtom(AtomType.MINUS)
        i += 1
      }
      else if (!Character.isLetterOrDigit(c)) {
        throw new IllegalArgumentException("Encountered unknown symbol '" + c + "' at position " + i)
      }
      else {
        val start: Int = i
        ((i+1) until expression.length)
          .map(i => (i, expression.charAt(i)))
          .takeWhile(c => c._2.isLetterOrDigit || c._2 == '.')
          .foreach(c => i = c._1)

        val parameterName: String = expression.substring(start, i)
        if (Character.isDigit(c)) {
          val intValue: Integer = parameterName.toInt
          list += new ExpressionAtom(AtomType.OBJECT, Option(new JsonPrimitive(intValue)))
        } else if ("true" == parameterName) {
          list += new ExpressionAtom(AtomType.OBJECT, Option(new JsonPrimitive(true)))
        } else if ("false" == parameterName) {
          list += new ExpressionAtom(AtomType.OBJECT, Option(new JsonPrimitive(false)))
        } else if ("null" == parameterName) {
          list += new ExpressionAtom(AtomType.NULL)
        } else {
          list += new ExpressionAtom(AtomType.PARAMETER, Option(new JsonPrimitive(parameterName)))
        }
      }
    }

    list.toList
  }

  def evaluate(objectToEvaluate: JsonElement): Boolean = {
    val evaluator: ScriptEvaluator = new ScriptEvaluator(atoms, objectToEvaluate)
    ScriptEvaluator.toBoolean(evaluator.evaluate)
  }
}