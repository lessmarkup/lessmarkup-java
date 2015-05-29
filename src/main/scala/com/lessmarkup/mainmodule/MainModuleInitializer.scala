/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.mainmodule

import com.lessmarkup.Constants
import com.lessmarkup.dataobjects._
import com.lessmarkup.dataobjects.migrations.Initial
import com.lessmarkup.interfaces.data.{DataObject, Migration}
import com.lessmarkup.interfaces.module.ModuleInitializer
import com.lessmarkup.interfaces.recordmodel.RecordModel
import com.lessmarkup.interfaces.structure.NodeHandlerFactory
import com.lessmarkup.userinterface.model.common.FlatPageSettingsModel
import com.lessmarkup.userinterface.model.configuration.{NodeAccessModel, NodeSettingsModel}
import com.lessmarkup.userinterface.model.global._
import com.lessmarkup.userinterface.model.user.{ChangePasswordModel, ForgotPasswordModel, LoginModel, RegisterModel}
import com.lessmarkup.userinterface.nodehandlers.DefaultRootNodeHandlerFactory
import com.lessmarkup.userinterface.nodehandlers.configuration.{ConfigurationRootNodeHandlerFactory, NodeListNodeHandlerFactory}
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration._

class MainModuleInitializer extends ModuleInitializer {

  def getName: String = "Main Module"

  def getModuleType: String = Constants.ModuleTypeMain

  def getModelTypes: Seq[Class[_ <: RecordModel[_]]] = {
    Seq(
      classOf[FlatPageSettingsModel],
      classOf[NodeAccessModel],
      classOf[NodeSettingsModel],
      classOf[CustomizationModel],
      classOf[EmailConfigurationModel],
      classOf[ModuleModel],
      classOf[UserBlockModel],
      classOf[UserGroupModel],
      classOf[UserModel],
      classOf[ChangePasswordModel],
      classOf[ForgotPasswordModel],
      classOf[LoginModel],
      classOf[RegisterModel],
      classOf[DatabaseConfigurationModel]).toList
  }

  def getDataObjectTypes: Seq[Class[_ <: DataObject]] = {
    Seq(
      classOf[Currency],
      classOf[EntityChangeHistory],
      classOf[FailedLoginHistory],
      classOf[Language],
      classOf[MigrationHistory],
      classOf[Module],
      classOf[Node],
      classOf[NodeAccess],
      classOf[SiteCustomization],
      classOf[SiteProperties],
      classOf[SuccessfulLoginHistory],
      classOf[TestMail],
      classOf[Translation],
      classOf[User],
      classOf[UserBlockHistory],
      classOf[UserGroup],
      classOf[UserGroupMembership],
      classOf[UserLoginIpAddress],
      classOf[UserPropertyDefinition],
      classOf[Smile]
    ).toList
  }

  def getNodeHandlerTypes: Seq[Class[_ <: NodeHandlerFactory]] = {
    Seq[Class[_ <: NodeHandlerFactory]](
      classOf[ConfigurationRootNodeHandlerFactory],
      classOf[DefaultRootNodeHandlerFactory],
      classOf[NodeListNodeHandlerFactory],
      classOf[EngineNodeHandlerFactory],
      classOf[EmailNodeHandlerFactory],
      classOf[ModulesNodeHandlerFactory],
      classOf[SiteCustomizeNodeHandlerFactory],
      classOf[SiteGroupsNodeHandlerFactory],
      classOf[SitePropertiesNodeHandlerFactory],
      classOf[SiteUsersNodeHandlerFactory]
    ).toList
  }

  def getMigrations: Seq[Class[_ <: Migration]] = {
    Seq(classOf[Initial])
  }
}
