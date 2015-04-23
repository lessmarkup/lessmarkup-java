/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lessmarkup;

/**
 *
 * @author User
 */
public final class Constants {
    
    public final class Engine {
        public static final String NO_SCRIPT_BLOCK = "<noscript><iframe style=\"width:100%;border:none;\" src=\"?noscript\"></iframe></noscript>";
    }
    
    public final class Data {
        public static final String ID_PROPERTY_NAME = "id";
    }
    
    public final class NodePath {
        public static final String CONFIGURATION = "configuration";
        public static final String PROFILE = "profile";
        public static final String FORGOT_PASSWORD = "forgot_password";
        public static final String USER_CARDS = "users";
        public static final String SYSTEM_ACTION = "action";
        public static final String ADMIN_LOGIN_DEFAULT_PAGE = "administrator";
    }
    
    public final class ModuleType {
        public static final String MAIN = "MainModule";
    }
    
    public final class Minify {
        public static final String JS_MINIFY = "minify.js";
        public static final String CSS_MINIFY = "minify.css";
    }
    
    public final class Encrypt {
        public static final String SYMMETRIC_CIPHER = "AES";
        public static final int SYMMETIC_KEY_SIZE = 128;
        public static final int SALT_LENGTH = 16;
        public static final String HASH_PROVIDER = "SHA";
    }
    
    public final class MailTemplates {
        public static final String PASSWORD_GENERATED_NOTIFICATION = "Views/EmailTemplates/PasswordGeneratedNotification.html";
        public static final String USER_NEW_USER_CREATED = "Views/EmailTemplates/UserNewUserCreated.html";
        public static final String VALIDATE_USER = "Views/EmailTemplates/ValidateUser.html";
        public static final String ADMIN_NEW_USER_CREATED = "Views/EmailTemplates/AdminNewUserCreated.html";
        public static final String RESET_PASSWORD = "Views/EmailTemplates/ResetPassword.html";
    }
    
    public final class ModuleActions {
        public static final String VALIDATE_ACCOUNT = "validate";
        public static final String CHANGE_PASSWORD = "password";
    }
}