/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.scripting

import com.google.gson.{JsonElement, JsonNull, JsonPrimitive}

object ScriptEvaluator {
  def toBoolean(result: JsonElement): Boolean = {
    if (result == null) {
      return false
    }
    if (result.isJsonArray) {
      return result.getAsJsonArray.size > 0
    }
    result.isJsonPrimitive && result.getAsBoolean
  }

  def toInt(value: JsonElement): Int = {
    if (value == null || !value.isJsonPrimitive) 0 else value.getAsInt
  }

  private def getOperationPriority(atomType: AtomType): Int = {
    atomType match {
      case AtomType.AND => 10
      case AtomType.OR => 10
      case AtomType.MINUS => 9
      case AtomType.PLUS => 9
      case AtomType.EQUAL => 8
      case AtomType.NOT_EQUAL => 8
      case _ => -1
    }
  }

  private def isBinaryOperator(atomType: AtomType): Boolean = {
    atomType match {
      case AtomType.AND => true
      case AtomType.OR => true
      case AtomType.EQUAL => true
      case AtomType.MINUS => true
      case AtomType.PLUS => true
      case AtomType.NOT_EQUAL => true
      case _ => false
    }
  }

  private def executeEqual(left: JsonElement, right: JsonElement): Boolean = {
    left == right
  }

  private def executeBinaryOperator(atomType: AtomType, left: JsonElement, right: JsonElement): JsonElement = {
    atomType match {
      case AtomType.AND =>
        val ret: Boolean = toBoolean(left) && toBoolean(right)
        new JsonPrimitive(ret)
      case AtomType.OR =>
        val ret: Boolean = toBoolean(left) || toBoolean(right)
        new JsonPrimitive(ret)
      case AtomType.EQUAL => new JsonPrimitive(executeEqual(left, right))
      case AtomType.MINUS =>
        val ret: Integer = toInt(left) - toInt(right)
        new JsonPrimitive(ret)
      case AtomType.PLUS =>
        val ret: Integer = toInt(left) + toInt(right)
        new JsonPrimitive(ret)
      case AtomType.NOT_EQUAL =>
        val ret: Boolean = !executeEqual(left, right)
        new JsonPrimitive(ret)
      case _ =>
        throw new IllegalArgumentException("atomType")
    }
  }

  private def isUnaryOperator(atomType: AtomType): Boolean = {
    atomType match {
      case AtomType.INVERSE => true
      case _ => false
    }
  }

  private def executeUnaryOperator(atomType: AtomType, operand: JsonElement): JsonElement = {
    atomType match {
      case AtomType.INVERSE =>
        val ret: Boolean = !toBoolean(operand)
        new JsonPrimitive(ret)
      case _ =>
        throw new IllegalArgumentException("atomType")
    }
  }

 }

class ScriptEvaluator(atoms: List[ExpressionAtom], objectToEvaluate: JsonElement) {
  def evaluate: JsonElement = evaluateGroup(atoms)

  private def evaluateFlat(atoms: List[ExpressionAtom]): JsonElement = {

    atoms.size match {
      case 1 =>
        val op = atoms.head
        op.atomType match {
        case AtomType.OBJECT => op.value.get
        case AtomType.NULL => JsonNull.INSTANCE
        case AtomType.PARAMETER =>
          val parameterName: String = op.value.get.getAsString
          if (!objectToEvaluate.isJsonObject) {
            throw new IllegalArgumentException("Unknown parameter '" + parameterName + "'")
          }
          objectToEvaluate.getAsJsonObject.get(parameterName)
        case a if ScriptEvaluator.isUnaryOperator(a) =>
          ScriptEvaluator.executeUnaryOperator(op.atomType, op.value.get)
        }
      case a if a >= 3 =>
        val operators = atoms.zipWithIndex.filter(o => ScriptEvaluator.isBinaryOperator(o._1.atomType))

        if (operators.isEmpty) {
          throw new IllegalArgumentException("Expected binary operator")
        }

        operators.maxBy(a => ScriptEvaluator.getOperationPriority(a._1.atomType)) match {
          case (op, i) =>
            ScriptEvaluator.executeBinaryOperator(op.atomType, evaluateFlat(atoms.take(i)), evaluateFlat(atoms.drop(i+1)))
        }
      case _ => throw new IllegalArgumentException("Unexpected atoms count")
    }
  }

  private def getGroupsWithLevels(atoms: List[ExpressionAtom]): List[(ExpressionAtom, Boolean, Int, Int)] = {
    var level = 0
    atoms.zipWithIndex.filter(a => a._1.atomType == AtomType.CLOSE || a._1.atomType == AtomType.OPEN).map {case (a, i) =>
      val open = a.atomType == AtomType.OPEN
      val currentLevel = level
      if (open) {
        level += 1
      } else {
        level -= 1
      }
      (a, open, currentLevel, i)
    }
  }

  private def evaluateGroup(atoms: List[ExpressionAtom]): JsonElement = {

    val groups = getGroupsWithLevels(atoms)

    if (groups.isEmpty) {
      evaluateFlat(atoms)
    } else {
      val openGroup = groups.find(g => g._2 && g._3 == 0)
      val closeGroup = groups.find(g => !g._2 && g._3 == 0)

      if (openGroup.isEmpty || closeGroup.isEmpty) {
        throw new IllegalArgumentException("Cannot find root open/close elements")
      }

      val groupStart = openGroup.get._4
      val groupEnd = closeGroup.get._4

      val parts = atoms.zipWithIndex.groupBy(_._2 match {
        case a if a < groupStart => 0
        case a if a > groupStart && a < groupEnd => 1
        case a if a > groupEnd => 2
        case _ => 3
      })

      val before = parts(0).map(_._1)
      val after = parts(2).map(_._1)
      val inside = parts(1).map(_._1)

      evaluateGroup(before ::: (new ExpressionAtom(AtomType.OBJECT, Option(evaluateGroup(inside))) :: after))
    }
  }
}