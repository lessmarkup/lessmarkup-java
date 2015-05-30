package com.lessmarkup.engine.data.dialects

import java.sql.Connection

object DatabaseLanguageDialectFactory {
  def createDialect(connection: Option[Connection]): DatabaseLanguageDialect = {

    if (connection.isEmpty) {
      return new MySqlDialect // TODO: create fake dialect class
    }

    val driverName = connection.get.getMetaData.getDriverName

    driverName match {
      case "HSQL Database Engine Driver" => new HsqldbDialect
      case _ => new MySqlDialect
    }
  }
}
