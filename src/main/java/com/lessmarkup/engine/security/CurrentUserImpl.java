/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.security;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.FailedLoginHistory;
import com.lessmarkup.dataobjects.SuccessfulLoginHistory;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.dataobjects.UserLoginIpAddress;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.exceptions.DatabaseException;
import com.lessmarkup.interfaces.module.Implements;
import com.lessmarkup.interfaces.security.CurrentUser;
import com.lessmarkup.interfaces.security.LoginTicket;
import com.lessmarkup.interfaces.security.UserSecurity;
import com.lessmarkup.interfaces.structure.Tuple;
import com.lessmarkup.interfaces.system.EngineConfiguration;
import com.lessmarkup.interfaces.system.RequestContext;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import com.lessmarkup.interfaces.system.UserCache;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.logging.Level;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.Cookie;
import org.apache.commons.net.util.Base64;

class CookieUserModel {
    private long userId;
    private String email;
    private String name;
    private String properties;
    private final List<Long> groups = new ArrayList<>();
    private boolean administrator;
    private boolean approved;
    private boolean fakeUser;
    private boolean emailConfirmed;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public List<Long> getGroups() {
        return groups;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isFakeUser() {
        return fakeUser;
    }

    public void setFakeUser(boolean fakeUser) {
        this.fakeUser = fakeUser;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }
}

@Implements(CurrentUser.class)
public class CurrentUserImpl implements CurrentUser {

    private final DomainModelProvider domainModelProvider;
    private final UserSecurity userSecurity;
    private CookieUserModel userData;

    @Inject
    public CurrentUserImpl(DomainModelProvider domainModelProvider, UserSecurity userSecurity) {
        this.domainModelProvider = domainModelProvider;
        this.userSecurity = userSecurity;
        userData = loadCurrentUser();
    }
    
    private boolean noGlobalAdminUser(DomainModel model) throws SQLException {
        User user = model.query()
                .from(User.class)
                .where("administrator = $ AND removed = $ AND (blocked = $ OR unblockTime < $)", true, false, false, OffsetDateTime.now())
                .firstOrDefault(User.class, Constants.DataIdPropertyName());
        return user == null;
    }
    
    private CookieUserModel loadCurrentUser() {
        RequestContext context = RequestContextHolder.getContext();
        EngineConfiguration engineConfiguration = context.getEngineConfiguration();
        
        Cookie cookie = context.getCookie(engineConfiguration.getAuthCookieName());
        
        if (cookie == null) {
            return null;
        }
        
        String cookieValue = cookie.getValue().replace('_', '/').replace('-', '+');
        
        LoginTicket ticket = userSecurity.decryptLoginTicket(cookieValue);

        return loadCurrentUser(ticket);
    }
    
    private CookieUserModel loadCurrentUser(LoginTicket ticket) {
        if (ticket == null) {
            return null;
        }

        RequestContext context = RequestContextHolder.getContext();
        EngineConfiguration engineConfiguration = context.getEngineConfiguration();
        
        if (ticket.getUserId() == -1 && ticket.getEmail().equals(engineConfiguration.getNoAdminName())) {
            try (DomainModel domainModel = domainModelProvider.create()) {
                if (noGlobalAdminUser(domainModel)) {
                    CookieUserModel model = new CookieUserModel();
                    model.setEmail(ticket.getEmail());
                    model.setName(ticket.getName());
                    model.setAdministrator(true);
                    model.setFakeUser(true);
                    model.setApproved(true);
                    model.setEmailConfirmed(true);
                    model.setUserId(-1);
                    return model;
                }
            } catch (SQLException ex) {
                LoggingHelper.getLogger(CurrentUserImpl.class).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        
        DataCache dataCache = DependencyResolver.resolve(DataCache.class);
        
        UserCache currentUser = dataCache.get(UserCache.class, OptionalLong.of(ticket.getUserId()));
        
        if (currentUser.isRemoved()) {
            LoggingHelper.getLogger(getClass()).info("Cannot find user " + ticket.getUserId() + " for current user");
        }
        
        if (currentUser.isBlocked()) {
            if (currentUser.getUnblockTime() == null || currentUser.getUnblockTime().isAfter(OffsetDateTime.now())) {
                LoggingHelper.getLogger(getClass()).info("User is blocked");
                return null;
            }
        }
        
        if (!currentUser.isAdministrator() && !dataCache.get(SiteConfiguration.class).getHasUsers()) {
            LoggingHelper.getLogger(getClass()).info("User functionality is disabled by configuration");
            return null;
        }
        
        if (!ticket.isPersistent()) {
            String encryptedTicket = userSecurity.encryptLoginTicket(ticket);
            Cookie cookie = new Cookie(engineConfiguration.getAuthCookieName(), encryptedTicket);
            cookie.setPath(engineConfiguration.getAuthCookiePath());
            cookie.setMaxAge(engineConfiguration.getAuthCookieTimeout() * 60);
            cookie.setHttpOnly(true);
            context.setCookie(cookie);
        }
        
        CookieUserModel model = new CookieUserModel();
        model.setEmail(ticket.getEmail());
        model.setName(ticket.getName());
        currentUser.getGroups().forEach(model.getGroups()::add);
        model.setAdministrator(currentUser.isAdministrator());
        model.setApproved(currentUser.isApproved());
        model.setEmailConfirmed(currentUser.isEmailConfirmed());
        model.setUserId(ticket.getUserId());
        model.setProperties(currentUser.getProperties());
        return model;
    }
    
    @Override
    public OptionalLong getUserId() {
        if (userData == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(userData.getUserId());
    }

    @Override
    public List<Long> getGroups() {
        if (userData == null) {
            return null;
        }
        return userData.getGroups();
    }

    @Override
    public JsonObject getProperties() {
        if (userData == null || userData.getProperties() == null) {
            return null;
        }
        
        JsonParser parser = new JsonParser();
        
        JsonElement element = parser.parse(userData.getProperties());
        
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        
        return null;
    }

    @Override
    public boolean isAdministrator() {
        return userData != null && userData.isAdministrator();
    }

    @Override
    public boolean isApproved() {
        return userData != null && userData.isApproved();
    }

    @Override
    public boolean isFakeUser() {
        return userData != null && userData.isFakeUser();
    }

    @Override
    public boolean emailConfirmed() {
        return userData != null && userData.isEmailConfirmed();
    }

    @Override
    public String getEmail() {
        return userData == null ? null : userData.getEmail();
    }

    @Override
    public String getUserName() {
        return userData == null ? null : userData.getName();
    }

    @Override
    public void logout() {
        RequestContext context = RequestContextHolder.getContext();
        EngineConfiguration configuration = context.getEngineConfiguration();
        Cookie cookie = new Cookie(configuration.getAuthCookieName(), "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath(configuration.getAuthCookiePath());
        context.setCookie(cookie);
        userData = null;
    }

    @Override
    public void refresh() {
        userData = loadCurrentUser();
    }

    private boolean loginUser(String email, String name, long userId, boolean savePassword)
    {
        LoggingHelper.getLogger(getClass()).info("Logging in user " + email);

        LoginTicket ticket = new LoginTicket();
        ticket.setEmail(email);
        ticket.setName(name);
        ticket.setUserId(userId);
        ticket.setPersistent(savePassword);
        String encryptedTicket = userSecurity.encryptLoginTicket(ticket);
        
        encryptedTicket = encryptedTicket.trim().replace('+', '-').replace('/', '_');
        
        RequestContext context = RequestContextHolder.getContext();
        EngineConfiguration configuration = context.getEngineConfiguration();
        Cookie cookie = new Cookie(configuration.getAuthCookieName(), encryptedTicket);
        cookie.setPath(configuration.getAuthCookiePath());
        cookie.setHttpOnly(true);
        
        if (!savePassword) {
            cookie.setMaxAge(configuration.getAuthCookieTimeout()*60);
        }

        context.setCookie(cookie);
        userData = loadCurrentUser(ticket);
        return true;
    }
    

    @Override
    public boolean loginWithPassword(String email, String password, boolean savePassword, boolean allowAdmin, boolean allowRegular, String encodedPassword) {
        LoggingHelper.getLogger(getClass()).info("Validating user '" + email + "'");

        DataCache dataCache = DependencyResolver.resolve(DataCache.class);

        if (!allowAdmin && !dataCache.get(SiteConfiguration.class).getHasUsers()) {
            LoggingHelper.getLogger(getClass()).info("Users functionality is disabled");
            return false;
        }

        if (!EmailCheck.isValidEmail(email)) {
            LoggingHelper.getLogger(getClass()).info("User '" + email + "' has invalid email");
            return false;
        }

        if ((encodedPassword == null || encodedPassword.length() == 0) && !TextValidator.checkPassword(password)) {
            LoggingHelper.getLogger(getClass()).info("Failed to pass password rules check");
            return false;
        }
        
        RequestContext requestContext = RequestContextHolder.getContext();
        EngineConfiguration engineConfiguration = requestContext.getEngineConfiguration();

        try (DomainModel model = domainModelProvider.create()) {
            if (allowAdmin && email.equals(engineConfiguration.getNoAdminName()) && noGlobalAdminUser(model)) {
                LoggingHelper.getLogger(getClass()).info("No admin defined and user email is equal to NoAdminName");

                if (!loginUser(email, email, -1, savePassword)) {
                    return false;
                }
                
                SuccessfulLoginHistory history = new SuccessfulLoginHistory();
                history.setAddress(requestContext.getRemoteAddress());
                history.setUserId(-2);
                history.setTime(OffsetDateTime.now());
                model.create(history);
                return true;
            }

            User user = model.query().from(User.class).where("email = $", email).firstOrDefault(User.class);

            if (user != null && user.isBlocked())
            {
                LoggingHelper.getLogger(getClass()).info("User is blocked");

                if (user.getUnblockTime() == null || user.getUnblockTime().isAfter(OffsetDateTime.now())) {
                    return false;
                }

                LoggingHelper.getLogger(getClass()).info("Unblock time is arrived, unblocking the user");
                user.setBlocked(false);
                user.setBlockReason(null);
                user.setUnblockTime(null);
                model.update(user);
                DependencyResolver.resolve(ChangeTracker.class).addChange(User.class, user.getId(), EntityChangeType.UPDATED, model);
            }

            if (user == null) {
                LoggingHelper.getLogger(getClass()).info("Cannot find user '" + email + "'");
                return false;
            }

            if (!checkPassword(OptionalLong.of(user.getId()), user.getPassword(), user.getSalt(), 
                    user.isBlocked(), user.isRemoved(), user.getRegistrationExpires(), password, encodedPassword)) {
                LoggingHelper.getLogger(getClass()).info("User '" + email + "' failed password check");
                return false;
            }

            if (user.isAdministrator()) {
                if (!allowAdmin) {
                    LoggingHelper.getLogger(getClass()).info("Expected admin but the user is not admin");
                    return false;
                }
            }
            else
            {
                if (!allowRegular)
                {
                    LoggingHelper.getLogger(getClass()).info("Expected regular user but the user is admin");
                    return false;
                }
            }

            if (!loginUser(email, user.getName(), user.getId(), savePassword)) {
                return false;
            }

            addSuccessfulLoginHistory(model, user.getId());

            model.update(user);

            return true;
        } catch (SQLException ex) {
            LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean loginWithOAuth(String provider, String providerUserId, boolean savePassword, boolean allowAdmin, boolean allowRegular) {
        LoggingHelper.getLogger(getClass()).info("Validating OAuth user");

        DataCache dataCache = DependencyResolver.resolve(DataCache.class);

        if (!allowAdmin && !dataCache.get(SiteConfiguration.class).getHasUsers()) {
            LoggingHelper.getLogger(getClass()).info("Users functionality is disabled");
            return false;
        }

        try (DomainModel model = domainModelProvider.create()) {
            User user = model.query().from(User.class).where("authProvider = $ AND authProviderUserId = $", provider, providerUserId).firstOrDefault(User.class);

            if (user != null && user.isBlocked()) {
                if (user.getUnblockTime() != null && user.getUnblockTime().isBefore(OffsetDateTime.now())) {
                    user.setBlocked(false);
                    user.setBlockReason(null);
                    user.setUnblockTime(null);
                    model.update(user);
                    DependencyResolver.resolve(ChangeTracker.class).addChange(User.class, user.getId(), EntityChangeType.UPDATED, model);
                }
                else {
                    user = null;
                }
            }

            if (user == null) {
                LoggingHelper.getLogger(getClass()).info("Cannot find valid user for authprovider '" + provider + "' and userid '" + providerUserId + "'");
                return false;
            }

            if (user.isAdministrator()) {
                if (!allowAdmin) {
                    LoggingHelper.getLogger(getClass()).info("User not administrator, cancelling login");
                    return false;
                }
            }
            else {
                if (!allowRegular) {
                    LoggingHelper.getLogger(getClass()).info("User is not administrator, cancelling login");
                    return false;
                }
            }

            if (!loginUser(user.getEmail(), user.getName(), user.getId(), savePassword)) {
                return false;
            }

            addSuccessfulLoginHistory(model, user.getId());

            return true;
        } catch (SQLException ex) {
            LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public void deleteSelf(String password) throws Exception {

        if (userData == null) {
            throw new Exception("Cannot find user");
        }

        try (DomainModel model = domainModelProvider.create()) {
            User user = model.query().from(User.class).where(Constants.DataIdPropertyName() + " = $ AND removed = $", userData.getUserId(), false).firstOrDefault(User.class);

            if (user == null) {
                throw new Exception("Cannot find user");
            }

            if (!checkPassword(OptionalLong.of(user.getId()), user.getPassword(), user.getSalt(), false, false, null, password, null)) {
                throw new Exception(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.INVALID_PASSWORD));
            }

            user.setRemoved(true);
            model.update(user);

            logout();
        }
    }

    @Override
    public boolean checkPassword(DomainModel domainModel, String password) throws SQLException {
        if (userData == null) {
            return false;
        }

        User user = domainModel.query().from(User.class).where(Constants.DataIdPropertyName() + " = $ AND removed = $",
                userData.getUserId(), false).firstOrDefault(User.class);

        return user != null &&
                checkPassword(OptionalLong.of(user.getId()),
                        user.getPassword(),
                        user.getSalt(),
                        user.isBlocked(),
                        user.isRemoved(),
                        user.getRegistrationExpires(),
                        password,
                        null);

    }

    private static String generateFakeSalt(MessageDigest hashAlgorithm, String email) {
        hashAlgorithm.update(email.getBytes());
        Cipher cipher = UserSecurityImpl.initializeCipher(DependencyResolver.resolve(DataCache.class), Cipher.ENCRYPT_MODE);
        if (cipher == null) {
            return null;
        }
        byte[] encryptedBytes;
        try {
            byte[] hash = hashAlgorithm.digest();
            if (hash == null) {
                return null;
            }
            encryptedBytes = cipher.doFinal(hash);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            LoggingHelper.getLogger(CurrentUserImpl.class).log(Level.SEVERE, null, ex);
            return null;
        }
        return Base64.encodeBase64String(encryptedBytes);
    }
    
    @Override
    public Tuple<String, String> getLoginHash(String email) throws NoSuchAlgorithmException, SQLException {
        email = email.trim();
        
        String hash1 = "";
        String hash2 = UserSecurityImpl.generateSalt();

        if (email.length() > 0) {
            try (DomainModel domainModel = domainModelProvider.create()) {
                User user = domainModel.query().from(User.class).where("email = $ AND removed = $", email, false).firstOrDefault(User.class, "salt");

                if (user != null) {
                    hash1 = user.getSalt();
                }
            }
        }
        
        MessageDigest digest = MessageDigest.getInstance(Constants.EncryptHashProvider());
        
        if (hash1.length() == 0) {
            hash1 = generateFakeSalt(digest, email);
        }
        
        return new Tuple<>(hash1, hash2);
    }

    public boolean checkPassword(OptionalLong userId, String userPassword, String userSalt, 
            boolean isBlocked, boolean isRemoved, OffsetDateTime registrationExpires, String password, String encodedPassword) {
        if (userId.isPresent() && (isBlocked || isRemoved)) {
            LoggingHelper.getLogger(getClass()).info("User is null or blocked or removed");
            return false;
        }
        
        RequestContext requestContext = RequestContextHolder.getContext();
        EngineConfiguration engineConfiguration = requestContext.getEngineConfiguration();
        
        String remoteAddress = requestContext.getRemoteAddress();

        if (remoteAddress == null || remoteAddress.length() == 0) {
            LoggingHelper.getLogger(getClass()).info("User remote address is not specified");
            return false;
        }

        OffsetDateTime timeLimit = OffsetDateTime.now().minusMinutes(engineConfiguration.getFailedAttemptsRememberMinutes());
        int maxAttemptCount = engineConfiguration.getMaximumFailedAttempts() * 2;

        try (DomainModel model = domainModelProvider.create())
        {
            if (!userId.isPresent()) {
                LoggingHelper.getLogger(getClass()).info("User is not found, logging failed attempt from address '" + remoteAddress + "'");
                FailedLoginHistory failedAttempt = new FailedLoginHistory();
                failedAttempt.setCreated(OffsetDateTime.now());
                failedAttempt.setAddress(remoteAddress);
                model.create(failedAttempt);
                return false;
            }

            int attemptCount = model.query().from(FailedLoginHistory.class).where("userId IS NULL AND address = $ AND created > $", remoteAddress, timeLimit).count();

            if (attemptCount >= maxAttemptCount) {
                LoggingHelper.getLogger(getClass()).info("User is exceeded failed attempt limit for remote address '" + remoteAddress + "'");
                FailedLoginHistory failedAttempt = new FailedLoginHistory();
                failedAttempt.setCreated(OffsetDateTime.now());
                failedAttempt.setAddress(remoteAddress);
                model.create(failedAttempt);
                return false;
            }

            if (registrationExpires != null && registrationExpires.isBefore(OffsetDateTime.now())) {
                LoggingHelper.getLogger(getClass()).info("User registration is expired, removing the user from users list");
                User u = model.query().from(User.class).where(Constants.DataIdPropertyName() + " = $", userId.getAsLong()).first(User.class);
                u.setRemoved(true);
                model.update(u);
                return false;
            }

            attemptCount = model.query().from(FailedLoginHistory.class).where("userId = $ AND created > $", userId.getAsLong(), timeLimit).count();

            if (attemptCount >= maxAttemptCount) {
                LoggingHelper.getLogger(getClass()).info("Found failed attempts for specified user which exceed maximum attempt count");
                FailedLoginHistory failedAttempt = new FailedLoginHistory();
                failedAttempt.setCreated(OffsetDateTime.now());
                failedAttempt.setAddress(remoteAddress);
                failedAttempt.setUserId(userId);
                model.create(failedAttempt);
                return false;
            }

            boolean passwordValid = "-".equals(userPassword) && "-".equals(userSalt);

            if (!passwordValid)
            {
                if (encodedPassword != null && encodedPassword.length() > 0) {
                    String[] split = encodedPassword.split(";");
                    if (split.length == 2 && split[0].trim().length() > 0  && split[1].trim().length() > 0) {
                        String pass2 = UserSecurityImpl.encodePassword(userPassword, split[0]);
                        passwordValid = pass2.equals(split[1]);
                    }
                }
                else
                {
                    if (UserSecurityImpl.encodePassword(password, userSalt).equals(userPassword))
                    {
                        passwordValid = true;
                    }
                }
            }

            if (passwordValid)
            {
                LoggingHelper.getLogger(getClass()).info("Password is recognized as valid");

                if (attemptCount > 0) {
                    model.query().deleteFrom(FailedLoginHistory.class, "userId = $ AND created > $", userId.getAsLong(), timeLimit);
                }

                if (registrationExpires != null) {
                    User u = model.query().from(User.class).where(Constants.DataIdPropertyName() + " = $", userId.getAsLong()).first(User.class);
                    u.setRegistrationExpires(null);
                    model.update(u);
                }

                return true;
            }

            LoggingHelper.getLogger(getClass()).info("Password is invalid, logging new failed attempt for the user");

            FailedLoginHistory failedAttempt = new FailedLoginHistory();
            failedAttempt.setAddress(remoteAddress);
            failedAttempt.setUserId(userId);
            failedAttempt.setCreated(OffsetDateTime.now());
            model.create(failedAttempt);
            
            return false;
        } catch (DatabaseException ex) {
            LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private static void addSuccessfulLoginHistory(DomainModel domainModel, long userId) throws SQLException {
        
        RequestContext requestContext = RequestContextHolder.getContext();
        
        SuccessfulLoginHistory history = new SuccessfulLoginHistory();
        history.setAddress(requestContext.getRemoteAddress());
        history.setTime(OffsetDateTime.now());
        history.setUserId(userId);
        
        domainModel.create(history);

        UserLoginIpAddress loginIpaddress = new UserLoginIpAddress();
        loginIpaddress.setUserId(userId);
        loginIpaddress.setCreated(OffsetDateTime.now());
        loginIpaddress.setAddress(requestContext.getRemoteAddress());
        
        domainModel.create(loginIpaddress);
    }
}
