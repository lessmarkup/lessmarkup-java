package com.lessmarkup.userinterface.nodehandlers.configuration;

import com.lessmarkup.dataobjects.User;
import com.lessmarkup.dataobjects.UserGroup;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.recordmodel.ModelCollection;
import com.lessmarkup.interfaces.recordmodel.PropertyCollectionManager;
import com.lessmarkup.userinterface.model.configuration.NodeAccessModel;
import com.lessmarkup.userinterface.model.configuration.NodeAccessModelCollectionManager;
import com.lessmarkup.userinterface.nodehandlers.common.RecordListNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class NodeAccessNodeHandler extends RecordListNodeHandler<NodeAccessModel> implements PropertyCollectionManager {

    private long nodeId;

    @Autowired
    public NodeAccessNodeHandler(DomainModelProvider domainModelProvider, DataCache dataCache) {
        super(domainModelProvider, dataCache, NodeAccessModel.class);
    }

    public void initialize(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    protected ModelCollection<NodeAccessModel> createCollection() {
        NodeAccessModelCollectionManager collectionManager = (NodeAccessModelCollectionManager) super.createCollection();
        collectionManager.initialize(nodeId);
        return collectionManager;
    }

    @Override
    public List<String> getCollection(DomainModel domainModel, String property, String searchText) {
        if (StringHelper.isNullOrWhitespace(searchText)) {
            throw new IllegalArgumentException("searchText");
        }

        searchText = "%" + searchText + "%";

        switch (property)
        {
            case "user": {
                List<String> ret = new ArrayList<>();
                domainModel.query()
                        .from(User.class).where("name LIKE $ OR email LIKE $", searchText, searchText)
                        .toList(User.class, "email")
                        .forEach(u -> ret.add(u.getEmail()));
                return ret;
            }
            case "group": {
                List<String> ret = new ArrayList<>();
                domainModel.query()
                        .from(UserGroup.class).where("name LIKE $", searchText)
                        .toList(UserGroup.class, "name")
                        .forEach(g -> ret.add(g.getName()));
                return ret;
            }
            default:
                return null;
        }
    }
}
