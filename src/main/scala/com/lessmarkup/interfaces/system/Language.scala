package com.lessmarkup.interfaces.system

trait Language {
  def getName: String
  def getIconId: Option[Long]
  def getShortName: String
  def getIsDefault: Boolean
  def getTranslations: Map[String, String]
}
