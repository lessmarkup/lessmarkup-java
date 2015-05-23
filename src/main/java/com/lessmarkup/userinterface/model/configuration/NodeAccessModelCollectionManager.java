package com.lessmarkup.userinterface.model.configuration;

import com.google.inject.Inject;
import com.lessmarkup.Constants;
import com.lessmarkup.dataobjects.Node;
import com.lessmarkup.dataobjects.NodeAccess;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.dataobjects.UserGroup;
import com.lessmarkup.interfaces.cache.EntityChangeType;
import com.lessmarkup.interfaces.data.ChangeTracker;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.data.QueryBuilder;
import com.lessmarkup.interfaces.recordmodel.EditableModelCollection;
import com.lessmarkup.interfaces.structure.NodeAccessType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalLong;

public class NodeAccessModelCollectionManager implements EditableModelCollection<NodeAccessModel> {
    private long nodeId;
    private final DomainModelProvider domainModelProvider;
    private final ChangeTracker changeTracker;

    @Inject
    public NodeAccessModelCollectionManager(DomainModelProvider domainModelProvider, ChangeTracker changeTracker) {
        this.domainModelProvider = domainModelProvider;
        this.changeTracker = changeTracker;
    }

    public void initialize(long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public List<Long> readIds(QueryBuilder query, boolean ignoreOrder) {
        return query.from(NodeAccess.class).where("nodeId = $", nodeId).toIdList();
    }

    @Override
    public int getCollectionId() {
        return domainModelProvider.getCollectionId(NodeAccess.class).getAsInt();
    }

    @Override
    public Collection<NodeAccessModel> read(QueryBuilder query, List<Long> ids) {
        List<String> idsString = new LinkedList<>();
        ids.forEach(s -> idsString.add(s.toString()));
        return query.from(NodeAccess.class, "na").where(String.format("na.nodeId = $ AND na." + Constants.DataIdPropertyName() + " IN (%s)", String.join(",", idsString)), nodeId)
            .leftJoin(User.class, "u", "u." + Constants.DataIdPropertyName() + " = na.userId")
            .leftJoin(UserGroup.class, "g", "g." + Constants.DataIdPropertyName() + " = na.groupId")
            .toList(NodeAccessModel.class, "na.accessType, u.email, g.name, na." + Constants.DataIdPropertyName() + " AccessId");
    }

    public boolean isFiltered() { return false; }

    @Override
    public void initialize(OptionalLong objectId, NodeAccessType nodeAccessType) {
    }

    @Override
    public NodeAccessModel createRecord() {
        return new NodeAccessModel();
    }

    @Override
    public void addRecord(NodeAccessModel record) {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            NodeAccess access = new NodeAccess();
            access.setAccessType(record.getAccessType());
            access.setNodeId(nodeId);

            if (record.getUser() != null && record.getGroup().length() > 0) {
                long userId = domainModel.query().from(User.class).where("email = $", record.getUser()).first(User.class, "Id").getId();
                access.setUserId(OptionalLong.of(userId));
            }

            if (record.getGroup() != null && record.getGroup().length() > 0) {
                long groupId = domainModel.query().from(UserGroup.class).where("name = $", record.getGroup()).first(UserGroup.class, "Id").getId();
                access.setGroupId(OptionalLong.of(groupId));
            }

            domainModel.create(access);
            changeTracker.addChange(Node.class, nodeId, EntityChangeType.UPDATED, domainModel);
            domainModel.completeTransaction();

            record.setAccessId(access.getId());
        }
    }

    @Override
    public void updateRecord(NodeAccessModel record) {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            NodeAccess access = domainModel.query().find(NodeAccess.class, record.getAccessId());
            access.setAccessType(record.getAccessType());

            if (record.getUser() != null && record.getUser().length() > 0) {
                long userId = domainModel.query().from(User.class).where("email = $", record.getUser()).first(User.class).getId();
                access.setUserId(OptionalLong.of(userId));
            }
            else {
                access.setUserId(OptionalLong.empty());
            }

            if (record.getGroup() != null && record.getGroup().length() > 0) {
                long groupId = domainModel.query().from(UserGroup.class).where("name = $", record.getGroup()).first(UserGroup.class).getId();
                access.setGroupId(OptionalLong.of(groupId));
            }
            else {
                access.setGroupId(OptionalLong.empty());
            }

            domainModel.update(access);
            changeTracker.addChange(Node.class, nodeId, EntityChangeType.UPDATED, domainModel);
            domainModel.completeTransaction();
        }
    }

    @Override
    public boolean deleteRecords(Collection<Long> recordIds) {
        try (DomainModel domainModel = domainModelProvider.createWithTransaction()) {
            boolean hasChanges = false;

            for (long recordId : recordIds) {
                domainModel.delete(NodeAccess.class, recordId);
                hasChanges = true;
            }

            if (hasChanges) {
                changeTracker.addChange(Node.class, nodeId, EntityChangeType.UPDATED, domainModel);
                domainModel.completeTransaction();
            }

            return hasChanges;
        }
    }
}
