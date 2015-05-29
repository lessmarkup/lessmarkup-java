/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.framework.nodehandlers

import com.google.gson.JsonObject
import com.lessmarkup.interfaces.annotations.NodeAccessType

class NodeHandlerConfiguration(val objectId: Option[Long],
                               val settings: Option[JsonObject],
                               val accessType: NodeAccessType,
                               val path: String,
                               val fullPath: String)
