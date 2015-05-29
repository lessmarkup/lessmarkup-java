/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface

import javax.websocket.{Endpoint, EndpointConfig, Session}

class WebSocketEndpoint extends Endpoint {
  def onOpen(session: Session, config: EndpointConfig) {
    throw new UnsupportedOperationException("Not supported yet.")
  }
}