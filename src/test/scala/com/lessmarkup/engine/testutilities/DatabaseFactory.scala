/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.testutilities

import java.sql.{Connection, Savepoint}

import com.lessmarkup.engine.data.ConnectionManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, BeforeAndAfterEach}

object DatabaseFactory {

  def getConnectionString: String = {
    "org.hsqldb.jdbcDriver;jdbc:hsqldb:mem:LessMarkup;user=SA;password="
  }

  private def getConnection: Connection = ConnectionManager.getConnection(getConnectionString)

  lazy val connection: Connection = {
    val connection = DatabaseFactory.getConnection
    connection.setAutoCommit(false)
    connection
  }
}

abstract class DatabaseFactory extends FlatSpec with BeforeAndAfterEach with MockFactory {

  private var savePoint: Option[Savepoint] = None

  def getConnection: Connection = {

    if (savePoint.isEmpty) {
      savePoint = Option(DatabaseFactory.connection.setSavepoint())
    }

    DatabaseFactory.connection
  }

  protected override def beforeEach(): Unit = {
    super.beforeEach()
    savePoint = None
  }

  protected override def afterEach(): Unit = {
    super.afterEach()
    if (savePoint.isDefined) {
      DatabaseFactory.connection.rollback(savePoint.get)
      savePoint = None
    }
  }
}
