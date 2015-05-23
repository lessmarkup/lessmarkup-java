package com.lessmarkup.userinterface.nodehandlers.user;

import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.structure.ChildHandlerSettings;
import com.lessmarkup.interfaces.structure.NodeAccessType;
import com.lessmarkup.userinterface.model.user.ForgotPasswordModel;
import com.lessmarkup.userinterface.nodehandlers.common.DialogNodeHandler;

import java.util.Objects;

public class ForgotPasswordNodeHandler extends DialogNodeHandler<ForgotPasswordModel> {

    @Inject
    public ForgotPasswordNodeHandler(DataCache dataCache) {
        super(dataCache, ForgotPasswordModel.class);
    }

    @Override
    protected String getApplyCaption() {
        return LanguageHelper.getText(Constants.ModuleTypeMain(), TextIds.RESTORE_PASSWORD);
    }

    @Override
    protected ForgotPasswordModel loadObject() {
        return DependencyResolver.resolve(ForgotPasswordModel.class);
    }

    @Override
    protected String saveObject(ForgotPasswordModel changedObject) {
        changedObject.submit(this, getFullPath());
        return null;
    }

    @Override
    public ChildHandlerSettings getChildHandler(String path) {
        if (path == null) {
            return null;
        }

        String[] parts = path.split("/");

        if (parts.length != 3 || !Objects.equals(parts[0], "ticket")) {
            return null;
        }

        ResetPasswordNodeHandler handler = DependencyResolver.resolve(ResetPasswordNodeHandler.class);
        handler.initialize(null, null, null, null, NodeAccessType.READ);
        handler.initialize(parts[1], parts[2]);

        ChildHandlerSettings ret = new ChildHandlerSettings();
        ret.setHandler(handler);
        ret.setPath(path);
        return ret;
    }
}
