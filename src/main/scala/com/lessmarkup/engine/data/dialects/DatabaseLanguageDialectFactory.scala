package com.lessmarkup.engine.data.dialects

import java.sql.Connection

object DatabaseLanguageDialectFactory {
  def createDialect(connection: Option[Connection]): DatabaseLanguageDialect = {

    if (connection.isEmpty) {
      return new MySqlDialect // TODO: create fake dialect class
    }

    connection.get.getMetaData.getDriverName match {
      case _ => new MySqlDialect
    }
  }
}
