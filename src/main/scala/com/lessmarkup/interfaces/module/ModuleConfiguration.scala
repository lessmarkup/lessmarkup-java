package com.lessmarkup.interfaces.module

import java.io.{IOException, InputStream}
import java.net.URL

import org.apache.commons.io.IOUtils

trait ModuleConfiguration {
  def getElements: List[String]

  def isSystem: Boolean

  def getUrl: URL

  def getModuleType: String

  def getClassLoader: ClassLoader

  def getInitializer: ModuleInitializer

  @throws(classOf[IOException])
  def getResourceAsStream(path: String): InputStream

  @throws(classOf[IOException])
  def getResourceAsBytes(path: String): Array[Byte] = {
    try {
      val stream: InputStream = getResourceAsStream(path)
      try {
        IOUtils.toByteArray(stream)
      } finally {
        if (stream != null) stream.close()
      }
    }
  }
}
