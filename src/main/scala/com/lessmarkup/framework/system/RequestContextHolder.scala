/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.system

import com.lessmarkup.interfaces.system.RequestContext

object RequestContextHolder {

  private val contextHolder = new ThreadLocal[RequestContext]

  def onRequestStarted(context: RequestContext): Unit = {
    contextHolder.set(context)
  }

  def onRequestFinished(): Unit = {
    contextHolder.remove()
  }

  def getContext: RequestContext = {
    contextHolder.get()
  }
}
