/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lessmarkup

object Constants {

  final val EngineNoScriptBlock: String = "<noscript><iframe style=\"width:100%;border:none;\" src=\"?noscript\"></iframe></noscript>"

  final val DataIdPropertyName: String = "id"

  final val NodePathConfiguration: String = "configuration"
  final val NodePathProfile: String = "profile"
  final val NodePathForgotPassword: String = "forgot_password"
  final val NodePathUserCards: String = "users"
  final val NodePathSystemAction: String = "action"
  final val NodePathAdminLoginDefaultPage: String = "administrator"

  final val ModuleTypeMain: String = "Main"

  final val EncryptSymmetricCipher: String = "AES"
  final val EncryptSymmetricKeySize: Int = 128
  final val EncryptSaltLength: Int = 16
  final val EncryptHashProvider: String = "SHA"

  final val MailTemplatesPasswordGeneratedNotification: String = "views/emailTemplates/passwordGeneratedNotification.html"
  final val MailTemplatesUserNewUserCreated: String = "views/emailTemplates/userNewUserCreated.html"
  final val MailTemplatesValidateUser: String = "views/emailTemplates/validateUser.html"
  final val MailTemplatesAdminNewUserCreated: String = "views/emailTemplates/adminNewUserCreated.html"
  final val MailTemplatesResetPassword: String = "views/emailTemplates/resetPassword.html"

  final val ModuleActionsValidateAccount: String = "validate"
  final val ModuleActionsChangePassword: String = "password"
}