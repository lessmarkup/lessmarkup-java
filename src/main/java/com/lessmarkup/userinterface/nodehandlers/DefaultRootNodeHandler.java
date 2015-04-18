package com.lessmarkup.userinterface.nodehandlers;

import com.lessmarkup.framework.nodehandlers.AbstractNodeHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DefaultRootNodeHandler extends AbstractNodeHandler {
    @Override
    public boolean isStatic() {
        return true;
    }
}
