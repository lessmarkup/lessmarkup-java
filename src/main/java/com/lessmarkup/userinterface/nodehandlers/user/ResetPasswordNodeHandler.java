package com.lessmarkup.userinterface.nodehandlers.user;

import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.security.UserSecurity;
import com.lessmarkup.interfaces.system.RequestContext;
import com.lessmarkup.userinterface.model.user.ChangePasswordModel;
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler;
import scala.Option;

import java.io.IOException;
import java.time.OffsetDateTime;

public class ResetPasswordNodeHandler extends DialogNodeHandler<ChangePasswordModel> {

    private String ticket;
    private String email;

    private final DomainModelProvider domainModelProvider;
    private final UserSecurity userSecurity;

    @Inject
    public ResetPasswordNodeHandler(DataCache dataCache, DomainModelProvider domainModelProvider, UserSecurity userSecurity) {
        super(dataCache, ChangePasswordModel.class);
        this.domainModelProvider = domainModelProvider;
        this.userSecurity = userSecurity;
    }

    @Override
    protected ChangePasswordModel loadObject() {
        return DependencyResolver.resolve(ChangePasswordModel.class);
    }

    @Override
    protected String saveObject(ChangePasswordModel changedObject) {
        Option<Object> userId = userSecurity.validatePasswordChangeToken(email, ticket);

        if (!userId.isDefined()) {
            LoggingHelper.getLogger(getClass()).info("Cannot change password: cannot get valid user id");
            return LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.PASSWORD_CHANGE_ERROR);
        }

        try (DomainModel domainModel = domainModelProvider.create()) {
            User user = domainModel.query().findOrDefaultJava(User.class, (Long)userId.get());

            if (user == null) {
                LoggingHelper.getLogger(getClass()).info("Cannot change password: user id=" + userId.get() + " does not exist");
                return LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.PASSWORD_CHANGE_ERROR);
            }

            scala.Tuple2<String, String> result = userSecurity.changePassword(changedObject.getPassword());

            user.setPasswordChangeToken(null);
            user.setPasswordChangeTokenExpires(null);
            user.setPassword(result._2());
            user.setEmailConfirmed(true);
            user.setSalt(result._1());
            user.setLastPasswordChanged(OffsetDateTime.now());
            user.setEmailConfirmed(true);

            domainModel.update(user);

            return LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.PASSWORD_CHANGED);
        }
    }

    public void initialize(String email, String ticket) {
        this.email = email;
        this.ticket = ticket;
    }

    @Override
    public boolean trySubmitResponse(String path) throws IOException {
        if (path != null) {
            return false;
        }

        Option<Object> userId = userSecurity.validatePasswordChangeToken(email, ticket);

        RequestContext requestContext = RequestContextHolder.getContext();

        if (!userId.isDefined()) {
            requestContext.sendError(404);
            return true;
        }

        return false;
    }
}
