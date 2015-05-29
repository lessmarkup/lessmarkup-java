/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.helpers

import java.util.logging.{Level, Logger}

object LoggingHelper {
  def getLogger(`type`: Class[_]): Logger = {
    Logger.getLogger(`type`.getName)
  }

  def logException(`type`: Class[_], e: Throwable) {
    getLogger(`type`).log(Level.SEVERE, "Exception occurred", e)
  }
}