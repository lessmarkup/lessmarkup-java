package com.lessmarkup.userinterface.nodehandlers.user

import java.time.OffsetDateTime

import com.lessmarkup.dataobjects.User
import com.lessmarkup.framework.helpers.{DependencyResolver, LanguageHelper, LoggingHelper}
import com.lessmarkup.framework.nodehandlers.NodeHandlerConfiguration
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.DataCache
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}
import com.lessmarkup.interfaces.security.UserSecurity
import com.lessmarkup.interfaces.structure.{NodeHandler, NodeHandlerFactory}
import com.lessmarkup.interfaces.system.RequestContext
import com.lessmarkup.userinterface.model.user.ChangePasswordModel
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler
import com.lessmarkup.{Constants, TextIds}

class ResetPasswordNodeHandlerFactory(dataCache: DataCache, domainModelProvider: DomainModelProvider, userSecurity: UserSecurity)
  extends NodeHandlerFactory {
  override def createNodeHandler(configuration: NodeHandlerConfiguration, arguments: Any*): NodeHandler = {
    if (arguments.length != 2) {
      throw new IllegalArgumentException
    }
    new ResetPasswordNodeHandler(dataCache, domainModelProvider, userSecurity, configuration, arguments(0).asInstanceOf, arguments(1).asInstanceOf)
  }
}

class ResetPasswordNodeHandler(
    dataCache: DataCache,
    domainModelProvider: DomainModelProvider,
    userSecurity: UserSecurity,
    configuration: NodeHandlerConfiguration,
    email: String,
    ticket: String
  )
  extends DialogNodeHandler[ChangePasswordModel](dataCache, classOf[ChangePasswordModel], configuration) {

  protected def loadObject: Option[ChangePasswordModel] = {
    Option(DependencyResolver.resolve(classOf[ChangePasswordModel]))
  }

  protected def saveObject(changedObject: Option[ChangePasswordModel]): String = {

    val userId = userSecurity.validatePasswordChangeToken(email, ticket)

    if (userId.isEmpty) {
      LoggingHelper.getLogger(getClass).info("Cannot change password: cannot get valid user id")
      return LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.PASSWORD_CHANGE_ERROR)
    }

    val domainModel: DomainModel = domainModelProvider.create
    try {
      val userOption = domainModel.query.find(classOf[User], userId.get)
      if (userOption.isEmpty) {
        LoggingHelper.getLogger(getClass).info("Cannot change password: user id=" + userId.get + " does not exist")
        return LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.PASSWORD_CHANGE_ERROR)
      }
      val user = userOption.get
      val result: (String, String) = userSecurity.changePassword(changedObject.get.getPassword)
      user.passwordChangeToken = None
      user.passwordChangeTokenExpires = None
      user.password = result._2
      user.emailConfirmed = true
      user.salt = result._1
      user.lastPasswordChanged = OffsetDateTime.now
      user.emailConfirmed = true
      domainModel.update(user)
      LanguageHelper.getText(Constants.ModuleTypeMain, TextIds.PASSWORD_CHANGED)
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  override def trySubmitResponse(path: String): Boolean = {
    if (path != null) {
      return false
    }
    val userId = userSecurity.validatePasswordChangeToken(email, ticket)
    val requestContext: RequestContext = RequestContextHolder.getContext
    if (userId.isEmpty) {
      requestContext.sendError(404)
      return true
    }
    false
  }
}