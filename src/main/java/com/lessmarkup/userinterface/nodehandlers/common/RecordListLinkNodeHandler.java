package com.lessmarkup.userinterface.nodehandlers.common;

import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.LanguageHelper;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.recordmodel.RecordModel;
import com.lessmarkup.interfaces.structure.ChildHandlerSettings;
import com.lessmarkup.interfaces.structure.NodeHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

public abstract class RecordListLinkNodeHandler<T extends RecordModel> extends RecordListNodeHandler<T> {

    static class CellLinkHandler {
        private String text;
        private Class<? extends NodeHandler> handlerType;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Class<? extends NodeHandler> getHandlerType() {
            return handlerType;
        }

        public void setHandlerType(Class<? extends NodeHandler> handlerType) {
            this.handlerType = handlerType;
        }
    }

    private final Map<String, CellLinkHandler> cellLinkHandlers = new HashMap<>();

    protected RecordListLinkNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache, Class<T> modelType) {
        super(domainModelProvider, dataCache, modelType);
    }

    protected <TH extends NodeHandler> void addCellLink(Class<TH> type, String text, String link) {
        CellLinkHandler handler = new CellLinkHandler();
        handler.setHandlerType(type);
        handler.setText(text);
        cellLinkHandlers.put(link, handler);
    }

    @Override
    public boolean hasChildren() {
        return !cellLinkHandlers.isEmpty();
    }

    @Override
    public ChildHandlerSettings getChildHandler(String path) {
        String[] split = path.split("/");
        if (split.length < 2)
        {
            return null;
        }

        long recordId;
        try {
            recordId = Long.parseLong(split[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        CellLinkHandler cellLinkHandler = cellLinkHandlers.get(split[1]);

        if (cellLinkHandler == null) {
            return null;
        }

        NodeHandler handler = DependencyResolver.resolve(cellLinkHandler.getHandlerType());

        if (handler == null) {
            return null;
        }

        String localPath = String.join("/", Arrays.stream(split).skip(2).toArray(String[]::new));

        handler.initialize(OptionalLong.of(recordId), null, split[0], getFullPath() + "/" + localPath, getAccessType());

        ChildHandlerSettings ret = new ChildHandlerSettings();
        ret.setPath(localPath);
        ret.setTitle(LanguageHelper.getFullTextId(getRecordModel().getModuleType(), cellLinkHandler.getText()));
        ret.setHandler(handler);
        ret.setId(OptionalLong.of(recordId));
        ret.setRest(localPath);

        return ret;
    }
}
