/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.mainmodule.initialization;

import com.lessmarkup.Constants;
import com.lessmarkup.dataobjects.*;
import com.lessmarkup.dataobjects.migrations.Initial;
import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.data.Migration;
import com.lessmarkup.interfaces.module.ModuleInitializer;
import com.lessmarkup.interfaces.module.ModuleIntegration;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.userinterface.model.common.FlatPageSettingsModel;
import com.lessmarkup.userinterface.model.configuration.NodeAccessModel;
import com.lessmarkup.userinterface.model.configuration.NodeSettingsModel;
import com.lessmarkup.userinterface.model.global.*;
import com.lessmarkup.userinterface.model.user.ChangePasswordModel;
import com.lessmarkup.userinterface.model.user.ForgotPasswordModel;
import com.lessmarkup.userinterface.model.user.LoginModel;
import com.lessmarkup.userinterface.model.user.RegisterModel;
import com.lessmarkup.userinterface.nodehandlers.DefaultRootNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.configuration.ConfigurationRootNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.configuration.NodeListNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration.EmailNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration.EngineNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration.ModulesNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration.SiteCustomizeNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration.SiteGroupsNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration.SitePropertiesNodeHandler;
import com.lessmarkup.userinterface.nodehandlers.globalconfiguration.SiteUsersNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
@Scope("prototype")
public class MainModuleInitializer implements ModuleInitializer {

    private final ModuleIntegration moduleIntegration;

    @Autowired
    public MainModuleInitializer(ModuleIntegration moduleIntegration) {
        this.moduleIntegration = moduleIntegration;
    }

    @Override
    public String getName() {
        return "Main Module";
    }

    @Override
    public String getModuleType() {
        return Constants.ModuleType.MAIN;
    }

    @Override
    public Collection<Class<? extends RecordModel>> getModelTypes() {
        Collection<Class<? extends RecordModel>> ret = new ArrayList<>();
        ret.add(FlatPageSettingsModel.class);
        ret.add(NodeAccessModel.class);
        ret.add(NodeSettingsModel.class);
        ret.add(CustomizationModel.class);
        ret.add(EmailConfigurationModel.class);
        ret.add(ModuleModel.class);
        ret.add(UserBlockModel.class);
        ret.add(UserGroupModel.class);
        ret.add(UserModel.class);
        ret.add(ChangePasswordModel.class);
        ret.add(ForgotPasswordModel.class);
        ret.add(LoginModel.class);
        ret.add(RegisterModel.class);
        ret.add(DatabaseConfigurationModel.class);
        return ret;
    }

    @Override
    public Collection<Class<? extends DataObject>> getDataObjectTypes() {
        Collection<Class<? extends DataObject>> ret = new ArrayList<>();
        ret.add(Currency.class);
        ret.add(EntityChangeHistory.class);
        ret.add(FailedLoginHistory.class);
        ret.add(Language.class);
        ret.add(MigrationHistory.class);
        ret.add(Module.class);
        ret.add(Node.class);
        ret.add(NodeAccess.class);
        ret.add(SiteCustomization.class);
        ret.add(SiteProperties.class);
        ret.add(SuccessfulLoginHistory.class);
        ret.add(TestMail.class);
        ret.add(Translation.class);
        ret.add(User.class);
        ret.add(UserBlockHistory.class);
        ret.add(UserGroup.class);
        ret.add(UserGroupMembership.class);
        ret.add(UserLoginIpAddress.class);
        ret.add(UserPropertyDefinition.class);
        ret.add(Smile.class);
        return ret;
    }

    @Override
    public Collection<Class<? extends NodeHandler>> getNodeHandlerTypes() {
        Collection<Class<? extends NodeHandler>> ret = new ArrayList<>();
        ret.add(ConfigurationRootNodeHandler.class);
        ret.add(DefaultRootNodeHandler.class);
        ret.add(NodeListNodeHandler.class);
        ret.add(EngineNodeHandler.class);
        ret.add(EmailNodeHandler.class);
        ret.add(ModulesNodeHandler.class);
        ret.add(SiteCustomizeNodeHandler.class);
        ret.add(SiteGroupsNodeHandler.class);
        ret.add(SitePropertiesNodeHandler.class);
        ret.add(SiteUsersNodeHandler.class);
        return ret;
    }

    @Override
    public Collection<Class<? extends Migration>> getMigrations() {
        Collection<Class<? extends Migration>> ret = new ArrayList<>();
        ret.add(Initial.class);
        return ret;
    }
}
