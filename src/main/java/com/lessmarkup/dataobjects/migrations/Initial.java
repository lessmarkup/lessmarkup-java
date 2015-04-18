package com.lessmarkup.dataobjects.migrations;

import com.lessmarkup.dataobjects.*;
import com.lessmarkup.interfaces.data.Migration;
import com.lessmarkup.interfaces.data.Migrator;

public class Initial implements Migration {
    @Override
    public String getId() {
        return "20150410_210800";
    }

    @Override
    public void migrate(Migrator migrator) {
        migrator.createTable(User.class);
        migrator.createTable(Currency.class);
        migrator.createTable(EntityChangeHistory.class);
        migrator.createTable(AttachedFile.class);
        migrator.createTable(Image.class);
        migrator.createTable(Language.class);
        migrator.createTable(Menu.class);
        migrator.createTable(Module.class);
        migrator.createTable(NodeAccess.class);
        migrator.createTable(Node.class);
        migrator.createTable(NodeUserData.class);
        migrator.createTable(SiteCustomization.class);
        migrator.createTable(Smile.class);
        migrator.createTable(SuccessfulLoginHistory.class);
        migrator.createTable(TestMail.class);
        migrator.createTable(Translation.class);
        migrator.createTable(UserBlockHistory.class);
        migrator.createTable(UserGroupMembership.class);
        migrator.createTable(UserGroup.class);
        migrator.createTable(UserLoginIpAddress.class);
        migrator.createTable(UserPropertyDefinition.class);
        migrator.createTable(SiteProperties.class);
        migrator.addDependency(Translation.class, Language.class);
        migrator.addDependency(NodeUserData.class, Node.class);
        migrator.addDependency(NodeUserData.class, User.class);
        migrator.addDependency(NodeAccess.class, Node.class);
        migrator.addDependency(UserBlockHistory.class, User.class);
        migrator.addDependency(UserBlockHistory.class, User.class, "BlockedByUserId");
        migrator.addDependency(UserGroupMembership.class, User.class);
        migrator.addDependency(UserGroupMembership.class, UserGroup.class);
        migrator.addDependency(UserLoginIpAddress.class, User.class);
    }
}
