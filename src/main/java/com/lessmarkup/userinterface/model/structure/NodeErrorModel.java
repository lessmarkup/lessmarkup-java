package com.lessmarkup.userinterface.model.structure;

import com.lessmarkup.Constants;
import com.lessmarkup.TextIds;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.system.RequestContext;

import java.io.IOException;

public class NodeErrorModel {
    public void initialize() {

    }

    public void handleRequest() throws IOException {
        RequestContext requestContext = RequestContextHolder.getContext();
        requestContext.addHeader("Content-Type", "text/plain");
        requestContext.getOutputStream().write(LanguageHelper.getText(Constants.ModuleType.MAIN, TextIds.UNKNOWN_ERROR).getBytes());
    }
}
