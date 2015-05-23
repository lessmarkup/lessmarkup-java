package com.lessmarkup.engine.data

import com.lessmarkup.framework.helpers.TypeHelper
import org.atteo.evo.inflector.English

import scala.collection.JavaConversions._

class TableMetadata(sourceType: Class[_]) {

  private val name: String = English.plural(sourceType.getSimpleName)
  private val columns = TypeHelper.getProperties(sourceType).map(p => (p.getName, p)).toMap

  def getName: String = {
    this.name
  }

  def getColumns = this.columns
}
