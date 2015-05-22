package com.lessmarkup.engine.data.dialects

import java.sql.Connection

object DatabaseLanguageDialectFactory {
  def createDialect(connection: Connection): DatabaseLanguageDialect = {
    connection.getMetaData.getDriverName match {
      case _ => new MySqlDialect
    }
  }
}