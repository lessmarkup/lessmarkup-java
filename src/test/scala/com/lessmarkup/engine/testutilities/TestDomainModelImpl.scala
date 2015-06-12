/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.testutilities

import java.sql.Connection

import com.lessmarkup.engine.data.DomainModelImpl

class TestDomainModelImpl(connection: Connection) extends DomainModelImpl(None, false) {
  protected override def createConnection: Option[Connection] = {
    Option(connection)
  }
  override def close(): Unit = {
    // do nothing
  }
}
