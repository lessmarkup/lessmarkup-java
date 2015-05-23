package com.lessmarkup.userinterface.model.user;

import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.recordmodel.InputField;
import com.lessmarkup.interfaces.recordmodel.InputFieldType;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.security.UserSecurity;
import com.lessmarkup.interfaces.structure.NodeHandler;
import com.lessmarkup.interfaces.system.MailSender;
import com.lessmarkup.interfaces.system.SiteConfiguration;

import java.util.OptionalLong;
import java.util.Random;

public class ForgotPasswordModel extends RecordModel<ForgotPasswordModel> {

    private String message;
    private String email;

    private final UserSecurity userSecurity;
    private final DataCache dataCache;
    private final DomainModelProvider domainModelProvider;
    private final MailSender mailSender;

    @Inject
    public ForgotPasswordModel(UserSecurity userSecurity, DataCache dataCache, DomainModelProvider domainModelProvider, MailSender mailSender) {
        super(TextIds.FORGOT_PASSWORD, true);
        this.userSecurity = userSecurity;
        this.dataCache = dataCache;
        this.domainModelProvider = domainModelProvider;
        this.mailSender = mailSender;
        message = LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.FORGOT_PASSWORD_MESSAGE);
    }

    @InputField(type = InputFieldType.LABEL)
    public void setMessage(String message) { this.message = message; }
    public String getMessage() { return this.message; }

    @InputField(type = InputFieldType.EMAIL, textId = TextIds.EMAIL)
    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return this.email; }

    public void submit(NodeHandler nodeHandler, String fullPath) {
        String siteName = dataCache.get(SiteConfiguration.class).getSiteName();

        if (StringHelper.isNullOrEmpty(siteName)) {
            return;
        }

        try (DomainModel domainModel = domainModelProvider.create()) {
            User user = domainModel.query().from(User.class).where("email = $", email).firstOrDefault(User.class);

            if (user == null) {
                try {
                    Thread.sleep(new Random(System.currentTimeMillis()).nextInt(400) + 100);
                    return;
                } catch (InterruptedException e) {
                    return;
                }
            }

            String resetUrl = String.format("%s/%s/%s", RequestContextHolder.getContext().getBasePath(), Constants.ModuleActionsChangePassword(), userSecurity.createPasswordChangeToken(user.getId()));

            ResetPasswordEmailModel model = new ResetPasswordEmailModel();
            model.setSiteName(siteName);
            model.setResetUrl(resetUrl);
            mailSender.sendMail(ResetPasswordEmailModel.class, OptionalLong.empty(), OptionalLong.of(user.getId()), email, Constants.MailTemplatesResetPassword(), model);
        }
    }
}
