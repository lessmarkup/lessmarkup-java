/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.nodehandlers.configuration

import com.google.inject.Inject
import com.lessmarkup.TextIds
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.interfaces.annotations.ConfigurationHandler
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.DomainModelProvider
import com.lessmarkup.userinterface.model.configuration.UserPropertyModel
import com.lessmarkup.userinterface.nodehandlers.common.RecordListLinkNodeHandler

@ConfigurationHandler(titleTextId = TextIds.USER_PROPERTIES)
class UserPropertiesNodeHandler @Inject() (
                                 domainModelProvider: DomainModelProvider,
                                 dataCache: DataCache,
                                 configuration: NodeHandlerConfiguration
                                 )
  extends RecordListLinkNodeHandler[UserPropertyModel](domainModelProvider, dataCache, classOf[UserPropertyModel], configuration)