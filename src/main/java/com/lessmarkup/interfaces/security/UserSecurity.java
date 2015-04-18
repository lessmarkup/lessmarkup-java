/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.interfaces.security;

import com.lessmarkup.interfaces.structure.Tuple;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.OptionalLong;

public interface UserSecurity {
    Tuple<String, String> changePassword(String password);
    long createUser(String username, String password, String email, boolean preApproved, boolean generatePassword);
    String createPasswordChangeToken(long userId);
    OptionalLong validatePasswordChangeToken(String email, String token);
    String createAccessToken(int collectionId, long entityId, int accessType, OptionalLong userId, Optional<OffsetDateTime> expirationTime);
    boolean validateAccessToken(String token, int collectionId, long entityId, int accessType, OptionalLong userId);
    String generateUniqueId();
    OptionalLong confirmUser(String validateSecret);
    String encryptObject(Object obj);
    <T> T decryptObject(Class<T> type, String encrypted);
    String encryptLoginTicket(LoginTicket ticket);
    LoginTicket decryptLoginTicket(String ticket);
}
