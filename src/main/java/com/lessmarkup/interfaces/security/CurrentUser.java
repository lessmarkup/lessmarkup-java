package com.lessmarkup.interfaces.security;

import com.google.gson.JsonObject;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.structure.Tuple;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.OptionalLong;

public interface CurrentUser {
    OptionalLong getUserId();
    List<Long> getGroups();
    JsonObject getProperties();
    boolean isAdministrator();
    boolean isApproved();
    boolean isFakeUser();
    boolean emailConfirmed();
    String getEmail();
    String getUserName();
    void logout();
    void refresh();
    boolean loginWithPassword(String email, String password, boolean savePassword, boolean allowAdmin, boolean allowRegular, String encodedPassword);
    boolean loginWithOAuth(String provider, String providerUserId, boolean savePassword, boolean allowAdmin, boolean allowRegular);
    void deleteSelf(String password) throws Exception;
    boolean checkPassword(DomainModel domainModel, String password) throws SQLException;
    Tuple<String, String> getLoginHash(String email) throws NoSuchAlgorithmException, SQLException;
}
