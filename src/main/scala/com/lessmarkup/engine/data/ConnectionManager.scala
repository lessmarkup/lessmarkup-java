/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.data

import java.sql.{Connection, DriverManager}

object ConnectionManager {
  def getConnection(connectionString: String): Connection = {
    val driverPart: Int = connectionString.indexOf(';')
    val connectionStringParsed = if (driverPart > 0 && driverPart < connectionString.indexOf(':')) {
      val driverClass: String = connectionString.substring(0, driverPart)
      Class.forName(driverClass)
      connectionString.substring(driverPart + 1)
    } else {
      connectionString
    }
    DriverManager.getConnection(connectionStringParsed)
  }
}