/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.security;

import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.dataobjects.UserGroup;
import com.lessmarkup.dataobjects.UserGroupMembership;
import com.lessmarkup.engine.security.models.GeneratedPasswordModel;
import com.lessmarkup.engine.security.models.NewUserCreatedModel;
import com.lessmarkup.engine.security.models.UserConfirmationMailTemplateModel;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.exceptions.DatabaseException;
import com.lessmarkup.interfaces.exceptions.UserValidationException;
import com.lessmarkup.interfaces.module.Implements;
import com.lessmarkup.interfaces.security.EntityAccessType;
import com.lessmarkup.interfaces.security.LoginTicket;
import com.lessmarkup.interfaces.security.UserSecurity;
import com.lessmarkup.interfaces.structure.Tuple;
import com.lessmarkup.interfaces.system.EngineConfiguration;
import com.lessmarkup.interfaces.system.MailSender;
import com.lessmarkup.interfaces.system.SiteConfiguration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.logging.Level;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.net.util.Base64;

@Implements(UserSecurity.class)
public class UserSecurityImpl implements UserSecurity {

    private final DomainModelProvider domainModelProvider;
    private final DataCache dataCache;
    private final MailSender mailSender;
    private final ChangeTracker changeTracker;

    @Inject
    public UserSecurityImpl(DomainModelProvider domainModelProvider, DataCache dataCache, MailSender mailSender, ChangeTracker changeTracker) {
        this.domainModelProvider = domainModelProvider;
        this.dataCache = dataCache;
        this.mailSender = mailSender;
        this.changeTracker = changeTracker;
    }
    
    public static String encodePassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(Constants.EncryptHashProvider());
            digest.update((salt+password).getBytes(StandardCharsets.UTF_8));
            return toHexString(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new CommonException(ex);
        }
    }
    
    @Override
    public Tuple<String, String> changePassword(String password) {
        String salt = generateSalt();
        String encodedPassword = encodePassword(password, salt);
        return new Tuple<>(salt, encodedPassword);
    }
    
    private static void validateNewUserProperties(String username, String password, String email, boolean generatePassword) {
        if (!TextValidator.checkUsername(username)) {
            throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.INVALID_USERNAME));
        }

        if (!generatePassword && !TextValidator.checkPassword(password)) {
            throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.INVALID_PASSWORD));
        }

        if (!generatePassword && !TextValidator.checkNewPassword(password)) {
            throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.PASSWORD_TOO_SIMPLE));
        }

        while (email != null && email.length() > 0) {
            char c = email.charAt(email.length() - 1);
            if (c != '.' && !Character.isWhitespace(c)) {
                break;
            }
            email = email.substring(0, email.length() - 1);
        }

        if (!TextValidator.checkTextField(email) || !EmailCheck.isValidEmail(email)) {
            throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.INVALID_EMAIL, email));
        }
    }

    private void checkUserExistence(String email, DomainModel domainModel) throws UserValidationException
    {
        User user = domainModel.query().from(User.class).where("email = $ AND isRemoved = $", email, false).firstOrDefault(User.class, "Id");

        if (user != null) {
            LoggingHelper.getLogger(getClass()).info(String.format("User with e-mail '%s' already exists", email));
            throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.INVALID_EMAIL, email));
        }
    }
    
    private User createUserObject(String username, String email) {
        User user = new User();
        
        user.setSalt(generateSalt());
        user.setEmail(email);
        user.setName(username);
        user.setRegistered(OffsetDateTime.now());
        user.setBlocked(false);
        user.setEmailConfirmed(false);
        user.setLastLogin(OffsetDateTime.now());
        user.setLastActivity(OffsetDateTime.now());

        return user;
    }
    
    public String generatePassword() {
        return generatePassword(8);
    }
    
    private final static String PASSWORD_DICTIONARY = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-!?";
    
    public String generatePassword(int passwordLength) {
        byte[] data = generateSaltBytes();

        String ret = "";

        for (int i = 0; i < passwordLength; i++) {
            ret += PASSWORD_DICTIONARY.charAt(data[i]%PASSWORD_DICTIONARY.length());
        }

        return ret;
    }
    
    private void addToDefaultGroup(DomainModel domainModel, User user) {
        String defaultGroup = dataCache.get(SiteConfiguration.class).getDefaultUserGroup();

        if (defaultGroup != null && defaultGroup.length() > 0) {
            UserGroup group = domainModel.query().from(UserGroup.class).where("Name = $", defaultGroup).firstOrDefault(UserGroup.class);

            if (group == null) {
                group = new UserGroup();
                group.setName(defaultGroup);
                domainModel.create(group);
            }

            UserGroupMembership membership = new UserGroupMembership();
            membership.setUserId(user.getId());
            membership.setUserGroupId(group.getId());
            domainModel.create(membership);
        }
    }

    private void sendGeneratedPassword(String email, String password, User user)
    {
        GeneratedPasswordModel notificationModel = new GeneratedPasswordModel();
        
        notificationModel.setLogin(email);
        notificationModel.setPassword(password);
        // TODO: site path is unknown
        notificationModel.setSiteLink("");
        notificationModel.setSiteName(dataCache.get(SiteConfiguration.class).getSiteName());

        mailSender.sendMail(GeneratedPasswordModel.class, OptionalLong.empty(), OptionalLong.of(user.getId()), null, Constants.MailTemplatesPasswordGeneratedNotification(), notificationModel);
    }
    
    @Override
    public long createUser(String username, String password, String email, boolean preApproved, boolean generatePassword) {
        validateNewUserProperties(username, password, email, generatePassword);

        User user;

        try (DomainModel domainModel = domainModelProvider.createWithTransaction())
        {
            checkUserExistence(email, domainModel);

            user = createUserObject(username, email);

            if (generatePassword)
            {
                if (!RequestContextHolder.getContext().getEngineConfiguration().isSmtpConfigured()) {
                    throw new IllegalArgumentException(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.SMTP_NOT_CONFIGURED));
                }

                user.setPassword(generatePassword());
                user.setPasswordAutoGenerated(true);
                user.setRegistrationExpires(OffsetDateTime.now().plusDays(1));
            }

            user.setPassword(encodePassword(password, user.getSalt()));

            try {
                domainModel.create(user);
            }
            catch (DatabaseException ex) {
                SQLException e = (SQLException) ex.getCause();
                if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601 || e.getErrorCode() == 2512) {
                    LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, e);
                    throw new UserValidationException(LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.NAME_OR_EMAIL_EXISTS));
                }

                throw ex;
            }

            changeTracker.addChange(User.class, user.getId(), EntityChangeType.ADDED, domainModel);

            addToDefaultGroup(domainModel, user);

            if (generatePassword) {
                sendGeneratedPassword(email, password, user);
            }

            SiteConfiguration siteConfiguration = dataCache.get(SiteConfiguration.class);

            if (preApproved) {
                // means the user is created manually by the administrator
                user.setEmailConfirmed(true);
                user.setApproved(true);
                domainModel.update(user);
                userNotifyCreated(user, password);
            }
            else
            {
                user.setValidateSecret(generateUniqueId());
                domainModel.update(user);

                if (!siteConfiguration.getAdminApprovesNewUsers()) {
                    user.setApproved(true);
                }

                sendConfirmationLink(user);
            }

            if (siteConfiguration.getAdminNotifyNewUsers()) {
                adminNotifyNewUsers(user, domainModel);
            }

            domainModel.completeTransaction();
        }

        return user.getId();
    }

    @Override
    public String createPasswordChangeToken(long userId) {
        LoggingHelper.getLogger(getClass()).log(Level.INFO, "Creating password validation token for {0}", userId);
        
        try (DomainModel domainModel = domainModelProvider.create()) {
            User user = domainModel.query().from(User.class).where(Constants.DataIdPropertyName() + " = $", userId).firstOrDefault(User.class);
            if (user == null) {
                return null;
            }
            
            user.setPasswordChangeToken(generateUniqueId());
            user.setPasswordChangeTokenExpires(OffsetDateTime.now().plusMinutes(10));
            domainModel.update(user);
            return user.getPasswordChangeToken();
        }
    }

    @Override
    public OptionalLong validatePasswordChangeToken(String email, String token) {
        if (token == null || token.length() == 0 || email == null || email.length() == 0) {
            return OptionalLong.empty();
        }
        
        LoggingHelper.getLogger(getClass()).log(Level.INFO, "Validating password change token {0}", token);
        
        try (DomainModel domainModel = domainModelProvider.create()) {
            User user = domainModel.query().from(User.class).where("email = $ AND passwordChangeToken = $", email, token).firstOrDefault(User.class);
            if (user == null) {
                LoggingHelper.getLogger(getClass()).info("Cannot validate password - cannot find user or token");
                return OptionalLong.empty();
            }
            if (user.getPasswordChangeTokenExpires() == null || user.getPasswordChangeTokenExpires().isBefore(OffsetDateTime.now())) {
                LoggingHelper.getLogger(getClass()).info("Password validation token is expired");
                return OptionalLong.empty();
            }
            LoggingHelper.getLogger(getClass()).info("Password validated ok");
            return OptionalLong.of(user.getId());
        }
    }
    
    private Cipher initializeCipher(int mode) {
        return initializeCipher(dataCache, mode);
    }
    
    public static Cipher initializeCipher(DataCache dataCache, int mode) {
        try {
            
            EngineConfiguration engineConfiguration = RequestContextHolder.getContext().getEngineConfiguration();
            
            String secretKeyText = engineConfiguration.getSessionKey();
            SecretKey secretKey;
            
            if (secretKeyText == null || secretKeyText.length() == 0) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(Constants.EncryptSymmetricCipher());
                keyGenerator.init(Constants.EncryptSymmetricKeySize());
                secretKey = keyGenerator.generateKey();
                secretKeyText = Base64.encodeBase64String(secretKey.getEncoded(), false);
                engineConfiguration.setSessionKey(secretKeyText);
            } else {
                secretKey = new SecretKeySpec(Base64.decodeBase64(secretKeyText), Constants.EncryptSymmetricCipher());
            }
            
            Cipher ret = Cipher.getInstance(Constants.EncryptSymmetricCipher());
            ret.init(mode, secretKey);
            return ret;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            return null;
        }
    }

    private final static int LOGIN_TICKET_VERSION = 1;
    private final static int LOGIN_TICKET_CONTROL_WORD = 12345;
    
    @Override
    public String encryptLoginTicket(LoginTicket ticket) {
        Cipher cipher = initializeCipher(Cipher.ENCRYPT_MODE);
        if (cipher == null) {
            return null;
        }
        
        try (ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
                ObjectOutputStream objectStream = new ObjectOutputStream(memoryStream)) {
            objectStream.writeInt(LOGIN_TICKET_CONTROL_WORD);
            objectStream.writeInt(LOGIN_TICKET_VERSION);
            OffsetDateTime currentTime = OffsetDateTime.now().plusMinutes(RequestContextHolder.getContext().getEngineConfiguration().getAuthCookieTimeout());
            objectStream.writeLong(currentTime.toInstant().toEpochMilli());
            objectStream.writeUTF(RequestContextHolder.getContext().getRemoteAddress());
            objectStream.writeLong(ticket.getUserId());
            objectStream.writeUTF(ticket.getName());
            objectStream.writeUTF(ticket.getEmail());
            objectStream.writeBoolean(ticket.isPersistent());
            objectStream.flush();
            byte[] rawData = memoryStream.toByteArray();
            byte[] encryptedData = cipher.doFinal(rawData);
            return Base64.encodeBase64String(encryptedData, false);
        } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public LoginTicket decryptLoginTicket(String encryptedTicket) {
        try {
            Cipher cipher = initializeCipher(Cipher.DECRYPT_MODE);
            if (cipher == null) {
                return null;
            }

            byte[] binaryData = Base64.decodeBase64(encryptedTicket);
            binaryData = cipher.doFinal(binaryData);

            try (ByteArrayInputStream memoryStream = new ByteArrayInputStream(binaryData);
                    ObjectInputStream objectStream = new ObjectInputStream(memoryStream)) {

                if (objectStream.readInt() != LOGIN_TICKET_CONTROL_WORD) {
                    return null;
                }

                if (objectStream.readInt() != LOGIN_TICKET_VERSION) {
                    return null;
                }

                OffsetDateTime expirationTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(objectStream.readLong()), ZoneOffset.UTC);

                String remoteAddress = objectStream.readUTF();

                if (!RequestContextHolder.getContext().getRemoteAddress().equals(remoteAddress)) {
                    return null;
                }

                LoginTicket ticket = new LoginTicket();
                ticket.setUserId(objectStream.readLong());
                ticket.setName(objectStream.readUTF());
                ticket.setEmail(objectStream.readUTF());
                ticket.setPersistent(objectStream.readBoolean());

                if (!ticket.isPersistent() && expirationTime.isBefore(OffsetDateTime.now())) {
                    return null;
                }

                return ticket;
            }
        } catch (Exception e) {
                e.printStackTrace();
                return null;
        }
    }
    
    public class AccessToken implements Serializable {
        private OptionalLong userId;
        private int collectionId;
        private long entityId;
        private EntityAccessType accessType;
        private OptionalLong ticks;

        /**
         * @return the userId
         */
        public OptionalLong getUserId() {
            return userId;
        }

        /**
         * @param userId the userId to set
         */
        public void setUserId(OptionalLong userId) {
            this.userId = userId;
        }

        /**
         * @return the collectionId
         */
        public int getCollectionId() {
            return collectionId;
        }

        /**
         * @param collectionId the collectionId to set
         */
        public void setCollectionId(int collectionId) {
            this.collectionId = collectionId;
        }

        /**
         * @return the entityId
         */
        public long getEntityId() {
            return entityId;
        }

        /**
         * @param entityId the entityId to set
         */
        public void setEntityId(long entityId) {
            this.entityId = entityId;
        }

        /**
         * @return the accessType
         */
        public EntityAccessType getAccessType() {
            return accessType;
        }

        /**
         * @param accessType the accessType to set
         */
        public void setAccessType(EntityAccessType accessType) {
            this.accessType = accessType;
        }

        /**
         * @return the ticks
         */
        public OptionalLong getTicks() {
            return ticks;
        }

        /**
         * @param ticks the ticks to set
         */
        public void setTicks(OptionalLong ticks) {
            this.ticks = ticks;
        }
    }
    
    @Override
    public String createAccessToken(int collectionId, long entityId, int accessType, OptionalLong userId, Optional<OffsetDateTime> expirationTime) {
        Cipher cipher = initializeCipher(Cipher.ENCRYPT_MODE);
        if (cipher == null) {
            return null;
        }
        
        try (ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
                ObjectOutputStream objectStream = new ObjectOutputStream(memoryStream)) {
            objectStream.writeInt(collectionId);
            objectStream.writeLong(entityId);
            objectStream.writeInt(accessType);
            objectStream.writeLong(userId.orElse(0));
            if (!expirationTime.isPresent()) {
                objectStream.writeLong(0);
            } else {
                objectStream.writeLong(expirationTime.get().toInstant().toEpochMilli());
            }
            objectStream.flush();
            byte[] rawData = memoryStream.toByteArray();
            byte[] encryptedData = cipher.doFinal(rawData);
            return Base64.encodeBase64String(encryptedData, false);
        } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
            throw new CommonException(e);
        }
    }

    @Override
    public boolean validateAccessToken(String token, int collectionId, long entityId, int accessType, OptionalLong userId) {
        Cipher cipher = initializeCipher(Cipher.DECRYPT_MODE);
        if (cipher == null) {
            return false;
        }
        
        try (ByteArrayInputStream memoryStream = new ByteArrayInputStream(Base64.decodeBase64(token));
                ObjectInputStream objectStream = new ObjectInputStream(memoryStream)) {

            int tokenCollectionId = objectStream.readInt();
            long tokenEntityId = objectStream.readLong();
            int tokenAccessType = objectStream.readInt();
            long tokenUserId = objectStream.readLong();
            long tokenExpirationTicks = objectStream.readLong();

            switch (tokenAccessType) {
                case EntityAccessType.READ:
                    if ((accessType != EntityAccessType.READ && accessType != EntityAccessType.EVERYONE) || !userId.isPresent() || userId.getAsLong() != tokenUserId) {
                        return false;
                    }
                    break;
                case EntityAccessType.READ_WRITE:
                    if (!userId.isPresent() || userId.getAsLong() != tokenUserId) {
                        return false;
                    }
                    break;
                case EntityAccessType.EVERYONE:
                    break;
                default:
                    return false;
            }

            return collectionId == tokenCollectionId
                    && entityId == tokenEntityId
                    && !(tokenExpirationTicks != 0 && OffsetDateTime.now().toInstant().toEpochMilli() > tokenExpirationTicks);

        } catch (IOException e) {
            LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    private static byte[] generateSaltBytes() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[Constants.EncryptSaltLength()];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
    
    public static String generateSalt() {
        return Base64.encodeBase64String(generateSaltBytes(), false);
    }
    
    private static final String HEX_CODES = "0123456789abcdef";
    
    public static void appendHex(byte b, StringBuilder builder) {
        builder.append(HEX_CODES.charAt(b>>4));
        builder.append(HEX_CODES.charAt(b&0xf));
    }
    
    public static String toHexString(byte[] values)
    {
        StringBuilder sb = new StringBuilder();
        for (byte value : values) {
            appendHex(value, sb);
        }
        return sb.toString();
    }
    

    @Override
    public String generateUniqueId() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.putLong(OffsetDateTime.now().toInstant().toEpochMilli());
        buffer.put(generateSaltBytes());
        StringBuilder builder = new StringBuilder();
        byte[] array = buffer.array();
        for (int i = 0; i < buffer.position(); i++) {
            appendHex(array[i], builder);
        }
        return builder.toString();
    }
    
    private void userNotifyCreated(User user, String password) {
        NewUserCreatedModel model = new NewUserCreatedModel();
        model.setEmail(user.getEmail());
        model.setPassword(password);
        model.setSiteName(dataCache.get(SiteConfiguration.class).getSiteName());

        mailSender.sendMail(NewUserCreatedModel.class, OptionalLong.empty(), OptionalLong.of(user.getId()), null, Constants.MailTemplatesUserNewUserCreated(), model);
    }
    
    private void sendConfirmationLink(User user) {
        String path = String.format("%s/%s/%s", RequestContextHolder.getContext().getBasePath(), Constants.ModuleActionsValidateAccount(), user.getValidateSecret());
        UserConfirmationMailTemplateModel confirmationModel = new UserConfirmationMailTemplateModel();
        confirmationModel.setLink(path);
        mailSender.sendMail(UserConfirmationMailTemplateModel.class, OptionalLong.empty(), OptionalLong.of(user.getId()), null, Constants.MailTemplatesValidateUser(), confirmationModel);
    }

    @Override
    public OptionalLong confirmUser(String validateSecret) {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction())
        {
            User user = domainModel.query().from(User.class).where("validateSecret = $ AND emailConfirmed = $", validateSecret, false).firstOrDefault(User.class);
            if (user == null)
            {
                return OptionalLong.empty();
            }

            user.setValidateSecret(null);
            user.setEmailConfirmed(true);

            if (!dataCache.get(SiteConfiguration.class).getAdminApprovesNewUsers()) {
                user.setApproved(true);
            }

            changeTracker.addChange(User.class, user.getId(), EntityChangeType.UPDATED, domainModel);
            domainModel.update(user);
            domainModel.completeTransaction();
            
            return OptionalLong.of(user.getId());
        }
    }

    private void adminNotifyNewUsers(User user, DomainModel domainModel) {
        NewUserCreatedModel model = new NewUserCreatedModel();
        model.setUserId(user.getId());
        model.setName(user.getName());
        model.setEmail(user.getEmail());

        domainModel.query()
                .from(User.class)
                .where("isAdministrator = $ AND isRemoved = $ AND isBlocked = $", true, false, false)
                .toList(User.class, Constants.DataIdPropertyName())
                .forEach(admin -> mailSender.sendMail(NewUserCreatedModel.class, OptionalLong.empty(), OptionalLong.of(admin.getId()),
                        null, Constants.MailTemplatesAdminNewUserCreated(), model));
    }
    
    @Override
    public String encryptObject(Object obj) {
        Cipher cipher = initializeCipher(Cipher.ENCRYPT_MODE);
        if (cipher == null) {
            return null;
        }
        
        try (ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
                ObjectOutputStream objectStream = new ObjectOutputStream(memoryStream)) {
            objectStream.writeObject(obj);
            byte[] rawData = memoryStream.toByteArray();
            byte[] encryptedData = cipher.doFinal(rawData);
            return Base64.encodeBase64String(encryptedData, false);
        } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
            LoggingHelper.getLogger(getClass()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T decryptObject(Class<T> type, String encrypted) {
        Cipher cipher = initializeCipher(Cipher.DECRYPT_MODE);
        if (cipher == null) {
            return null;
        }
        
        byte[] data = Base64.decodeBase64(encrypted);
        try {
            data = cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            throw new CommonException(ex);
        }
        
        try (ByteArrayInputStream memoryStream = new ByteArrayInputStream(data);
                ObjectInputStream objectStream = new ObjectInputStream(memoryStream)) {
            
            return (T) objectStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new CommonException(e);
        }
    }
}
