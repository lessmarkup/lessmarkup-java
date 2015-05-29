/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.structure

import com.lessmarkup.framework.helpers.LanguageHelper
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.system.RequestContext
import com.lessmarkup.{Constants, TextIds}

class NodeErrorModel {

  def handleRequest() {
    val requestContext: RequestContext = RequestContextHolder.getContext
    requestContext.addHeader("Content-Type", "text/plain")
    requestContext.getOutputStream.write(LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.UNKNOWN_ERROR).getBytes)
  }
}
