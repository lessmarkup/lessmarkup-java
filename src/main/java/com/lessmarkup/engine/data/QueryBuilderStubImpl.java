package com.lessmarkup.engine.data;

import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.data.QueryBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class QueryBuilderStubImpl implements QueryBuilder {
    @Override
    public <T extends DataObject> QueryBuilder from(Class<T> type, String name) {
        return this;
    }

    @Override
    public <T extends DataObject> QueryBuilder join(Class<T> type, String name, String on) {
        return this;
    }

    @Override
    public <T extends DataObject> QueryBuilder leftJoin(Class<T> type, String name, String on) {
        return this;
    }

    @Override
    public <T extends DataObject> QueryBuilder rightJoin(Class<T> type, String name, String on) {
        return this;
    }

    @Override
    public QueryBuilder where(String filter, Object... args) {
        return this;
    }

    @Override
    public QueryBuilder whereIds(Collection<Long> ids) {
        return this;
    }

    @Override
    public QueryBuilder orderBy(String column) {
        return this;
    }

    @Override
    public QueryBuilder orderByDescending(String column) {
        return this;
    }

    @Override
    public QueryBuilder groupBy(String column) {
        return this;
    }

    @Override
    public QueryBuilder limit(int from, int count) {
        return this;
    }

    @Override
    public <T extends DataObject> T find(Class<T> type, long id) {
        throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public <T extends DataObject> T findOrDefault(Class<T> type, long id) {
        return null;
    }

    @Override
    public <T extends DataObject> List<T> execute(Class<T> type, String sql, Object... args) {
        return new ArrayList<>();
    }

    @Override
    public boolean executeNonQuery(String sql, Object... args) {
        return false;
    }

    @Override
    public <T> T executeScalar(Class<T> type, String sql, Object... args) {
        return null;
    }

    @Override
    public <T> List<T> toList(Class<T> type, String selectText) {
        return new ArrayList<>();
    }

    @Override
    public List<Long> toIdList() {
        return new ArrayList<>();
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public <T> T first(Class<T> type, String selectText) {
        throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public <T> T firstOrDefault(Class<T> type, String selectText) {
        return null;
    }

    @Override
    public QueryBuilder createNew() {
        return this;
    }

    @Override
    public <T extends DataObject> boolean deleteFrom(Class<T> type, String filter, Object... args) {
        return false;
    }
}
