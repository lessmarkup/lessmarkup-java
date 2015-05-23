package com.lessmarkup.userinterface.model.user;

import com.google.inject.Inject;
import com.lessmarkup.TextIds;
import com.lessmarkup.dataobjects.User;
import com.lessmarkup.interfaces.data.DomainModelProvider;
import com.lessmarkup.interfaces.data.QueryBuilder;
import com.lessmarkup.interfaces.recordmodel.ModelCollection;
import com.lessmarkup.interfaces.recordmodel.RecordColumn;
import com.lessmarkup.interfaces.recordmodel.RecordModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class UserCardModelCollection implements ModelCollection<UserCardModel> {

    private final DomainModelProvider domainModelProvider;

    @Inject
    public UserCardModelCollection(DomainModelProvider domainModelProvider) {
        this.domainModelProvider = domainModelProvider;
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
    public Collection<UserCardModel> read(QueryBuilder queryBuilder, List<Long> ids) {
        Collection<UserCardModel> ret = new ArrayList<>();
        for (User user : queryBuilder
                .from(User.class)
                .whereJava("removed = $", Collections.singletonList(false))
                .toListJava(User.class)) {
            UserCardModel model = new UserCardModel();
            model.setId(user.getId());
            model.setName(user.getName());
            model.setTitle(user.getTitle());
            model.setSignature(user.getSignature());
            ret.add(model);
        }
        return ret;
    }
}

public class UserCardModel extends RecordModel<UserCardModel> {

    private String name;
    private String title;
    private String signature;

    public UserCardModel() {
        super(UserCardModelCollection.class, User.class);
    }

    @RecordColumn(textId = TextIds.NAME)
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    @RecordColumn(textId = TextIds.TITLE)
    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    @RecordColumn(textId = TextIds.SIGNATURE)
    public void setSignature(String signature) { this.signature = signature; }
    public String getSignature() { return signature; }
}
