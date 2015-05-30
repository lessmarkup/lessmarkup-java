package com.lessmarkup.dataobjects.migrations

import com.lessmarkup.dataobjects._
import com.lessmarkup.interfaces.data.{Migration, Migrator}

class Initial extends Migration {
  def getId: String = {
    "20150410_210800"
  }

  def migrate(migrator: Migrator) {
    migrator.createTable(classOf[User])
    migrator.createTable(classOf[Currency])
    migrator.createTable(classOf[EntityChangeHistory])
    migrator.createTable(classOf[AttachedFile])
    migrator.createTable(classOf[Image])
    migrator.createTable(classOf[Language])
    migrator.createTable(classOf[Menu])
    migrator.createTable(classOf[Module])
    migrator.createTable(classOf[NodeAccess])
    migrator.createTable(classOf[Node])
    migrator.createTable(classOf[NodeUserData])
    migrator.createTable(classOf[SiteCustomization])
    migrator.createTable(classOf[Smile])
    migrator.createTable(classOf[SuccessfulLoginHistory])
    migrator.createTable(classOf[TestMail])
    migrator.createTable(classOf[Translation])
    migrator.createTable(classOf[UserBlockHistory])
    migrator.createTable(classOf[UserGroupMembership])
    migrator.createTable(classOf[UserGroup])
    migrator.createTable(classOf[UserLoginIpAddress])
    migrator.createTable(classOf[UserPropertyDefinition])
    migrator.createTable(classOf[SiteProperties])
    migrator.addDependency(classOf[Translation], classOf[Language])
    migrator.addDependency(classOf[NodeUserData], classOf[Node])
    migrator.addDependency(classOf[NodeUserData], classOf[User])
    migrator.addDependency(classOf[NodeAccess], classOf[Node])
    migrator.addDependency(classOf[UserBlockHistory], classOf[User])
    migrator.addDependency(classOf[UserBlockHistory], classOf[User], "blockedByUserId")
    migrator.addDependency(classOf[UserGroupMembership], classOf[User])
    migrator.addDependency(classOf[UserGroupMembership], classOf[UserGroup])
    migrator.addDependency(classOf[UserLoginIpAddress], classOf[User])
  }
}
