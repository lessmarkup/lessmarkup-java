/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.userinterface.model.global;

import com.google.inject.Inject;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.data.QueryBuilder;
import com.lessmarkup.interfaces.recordmodel.*;
import com.lessmarkup.interfaces.security.UserSecurity;
import com.lessmarkup.interfaces.structure.Tuple;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class UserModelCollection implements EditableModelCollection<UserModel> {

    private final DomainModelProvider domainModelProvider;
    private final UserSecurity userSecurity;
    private final ChangeTracker changeTracker;

    @Inject
    public UserModelCollection(DomainModelProvider domainModelProvider, UserSecurity userSecurity, ChangeTracker changeTracker) {
        this.domainModelProvider = domainModelProvider;
        this.userSecurity = userSecurity;
        this.changeTracker = changeTracker;
    }

    @Override
    public List<Long> readIds(QueryBuilder query, boolean ignoreOrder) {
        return query
                .from(User.class)
                .whereJava("removed = $", Collections.singletonList(false))
                .toIdListJava();
    }

    @Override
    public int getCollectionId() {
        return domainModelProvider.getCollectionId(User.class).getAsInt();
    }

    @Override
    public Collection<UserModel> read(QueryBuilder queryBuilder, List<Long> ids) {
        Collection<UserModel> ret = new ArrayList<>();
        for (User user : queryBuilder
                .from(User.class)
                .whereJava("removed = $", Collections.singletonList(false))
                .toListJava(User.class)) {
            UserModel model = new UserModel();
            model.setBlocked(user.isBlocked());
            model.setName(user.getName());
            model.setAdministrator(user.isAdministrator());
            model.setApproved(user.isApproved());
            model.setEmail(user.getEmail());
            model.setEmailConfirmed(user.isEmailConfirmed());
            model.setSignature(user.getSignature());
            ret.add(model);
        }
        return ret;
    }

    @Override
    public UserModel createRecord() {
        return DependencyResolver.resolve(UserModel.class);
    }

    @Override
    public void addRecord(UserModel record) {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            User user = new User();
            user.setEmail(record.getEmail());
            user.setName(record.getName());
            user.setRegistered(OffsetDateTime.now());
            user.setBlocked(false);
            user.setEmailConfirmed(true);
            user.setApproved(true);
            user.setLastPasswordChanged(OffsetDateTime.now());
            user.setAdministrator(record.isAdministrator());
            user.setSignature(record.getSignature());

            Tuple<String, String> change = userSecurity.changePassword(record.getPassword());

            user.setPasswordChangeToken(null);
            user.setPassword(change.getValue2());
            user.setSalt(change.getValue1());
            user.setPasswordAutoGenerated(false);

            domainModel.create(user);

            changeTracker.addChange(User.class, user, EntityChangeType.ADDED, domainModel);

            domainModel.completeTransaction();

            record.setPassword(null);
            record.setId(user.getId());
        }
    }

    @Override
    public void updateRecord(UserModel record) {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            User user = domainModel.query().from(User.class).findJava(User.class, record.getId());

            user.setName(record.getName());
            user.setEmail(record.getEmail());
            user.setAdministrator(record.isAdministrator());
            user.setSignature(record.getSignature());

            if (record.getPassword() != null && record.getPassword().length() > 0) {
                Tuple<String, String> change = userSecurity.changePassword(record.getPassword());
                user.setPassword(change.getValue2());
                user.setSalt(change.getValue1());
                user.setPasswordChangeToken(null);
                user.setPasswordChangeTokenExpires(null);
                user.setPasswordAutoGenerated(false);
                user.setLastPasswordChanged(OffsetDateTime.now());
                changeTracker.addChange(User.class, user, EntityChangeType.UPDATED, domainModel);
                domainModel.update(user);
                domainModel.completeTransaction();
                record.setPassword(null);
            }
        }
    }

    @Override
    public boolean deleteRecords(Collection<Long> recordIds) {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            for (long userId : recordIds) {
                domainModel.delete(User.class, userId);
                changeTracker.addChange(User.class, userId, EntityChangeType.REMOVED, domainModel);
            }
            domainModel.completeTransaction();
        }
        return true;
    }
}

public class UserModel extends RecordModel<UserModel> {

    private String name;
    private String email;
    private String password;
    private boolean administrator;
    private boolean validated;
    private boolean approved;
    private boolean emailConfirmed;
    private boolean blocked;
    private String signature;

    public UserModel() {
        super(TextIds.USER, UserModelCollection.class, User.class);
    }

    @RecordColumn(textId = TextIds.USER_NAME)
    @InputField(textId = TextIds.USER_NAME, type = InputFieldType.TEXT, required = true)
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    @RecordColumn(textId = TextIds.USER_EMAIL)
    @InputField(textId = TextIds.USER_EMAIL, type = InputFieldType.EMAIL, required = true)
    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }

    @InputField(textId = TextIds.PASSWORD, type = InputFieldType.PASSWORD_REPEAT, required = true)
    public void setPassword(String password) { this.password = password; }
    public String getPassword() { return password; }

    @RecordColumn(textId = TextIds.IS_ADMINISTRATOR)
    @InputField(textId = TextIds.IS_ADMINISTRATOR, type = InputFieldType.CHECK_BOX, defaultValue = "false")
    public void setAdministrator(boolean administrator) { this.administrator = administrator; }
    public boolean isAdministrator() { return administrator; }

    public void setValidated(boolean validated) { this.validated = validated; }
    public boolean isValidated() { return validated; }

    public void setApproved(boolean approved) { this.approved = approved; }
    public boolean isApproved() { return approved; }

    public void setEmailConfirmed(boolean emailConfirmed) { this.emailConfirmed = emailConfirmed; }
    public boolean isEmailConfirmed() { return emailConfirmed; }

    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public boolean isBlocked() { return blocked; }

    @InputField(textId = TextIds.SIGNATURE, type = InputFieldType.RICH_TEXT)
    public void setSignature(String signature) { this.signature = signature; }
    public String getSignature() { return signature; }
}
