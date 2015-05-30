/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.global

import com.lessmarkup.engine.data.ConnectionManager
import com.lessmarkup.engine.data.migrate.MigrateEngine
import com.lessmarkup.framework.helpers.{DependencyResolver, LanguageHelper, StringHelper}
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.annotations.{ActionAccess, InputField, InputFieldType, NodeAccessType}
import com.lessmarkup.interfaces.exceptions.CommonException
import com.lessmarkup.interfaces.recordmodel.RecordModel
import com.lessmarkup.interfaces.system.EngineConfiguration
import com.lessmarkup.{Constants, TextIds}

class DatabaseConfigurationModel extends RecordModel[DatabaseConfigurationModel](TextIds.DATABASE_CONFIGURATION) {

  @InputField(fieldType = InputFieldType.TEXT, textId = TextIds.CONNECTION_STRING, required = true)
  var database: String = null

  private def checkConnection() {
    ConnectionManager.getConnection(this.database).getSchema
  }

  @ActionAccess(minimumAccess = NodeAccessType.READ) def save: String = {
    try {
      checkConnection()
      val migrateEngine: MigrateEngine = DependencyResolver(classOf[MigrateEngine])
      migrateEngine.execute(this.database)
      val engineConfiguration: EngineConfiguration = RequestContextHolder.getContext.getEngineConfiguration
      engineConfiguration.setConnectionString(this.database)
    }
    catch {
      case e: Exception =>
        val errorMessage: String = LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.DATABASE_CHANGE_ERROR, StringHelper.getMessage(e))
        throw new CommonException(errorMessage)
    }
    LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.DATABASE_CHANGE_SUCCESS)
  }
}
