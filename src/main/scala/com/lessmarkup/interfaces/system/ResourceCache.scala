package com.lessmarkup.interfaces.system

import com.lessmarkup.interfaces.cache.CacheHandler

trait ResourceCache extends CacheHandler {
  def resourceExists(path: String): Boolean

  def readBytes(path: String): Option[Array[Byte]]

  def readText(path: String): Option[String]

  def parseText(path: String): Option[String]

  @Deprecated
  def parseTextJava(path: String): String = {
    parseText(path).getOrElse(null.asInstanceOf[String])
  }

  @Deprecated
  def readTextJava(path: String): String = {
    readText(path).getOrElse(null.asInstanceOf[String])
  }

  @Deprecated
  def readBytesJava(path: String): Array[Byte] = {
    readBytes(path).getOrElse(null.asInstanceOf[Array[Byte]])
  }

}