package com.lessmarkup.interfaces.data;

import java.util.Collection;
import java.util.List;

public interface QueryBuilder {
    <T extends DataObject> QueryBuilder from(Class<T> type, String name);
    default <T extends DataObject> QueryBuilder from(Class<T> type) {
        return from(type, null);
    }
    <T extends DataObject> QueryBuilder join(Class<T> type, String name, String on);
    <T extends DataObject> QueryBuilder leftJoin(Class<T> type, String name, String on);
    <T extends DataObject> QueryBuilder rightJoin(Class<T> type, String name, String on);
    QueryBuilder where(String filter, Object ... args);
    QueryBuilder whereId(Long id);
    QueryBuilder whereIds(Collection<Long> ids);
    QueryBuilder orderBy(String column);
    QueryBuilder orderByDescending(String column);
    QueryBuilder groupBy(String column);
    QueryBuilder limit(int from, int count);
    <T extends DataObject> T find(Class<T> type, long id);
    <T extends DataObject> T findOrDefault(Class<T> type, long id);
    <T extends DataObject> List<T> execute(Class<T> type, String sql, Object ... args);
    boolean executeNonQuery(String sql, Object ... args);
    <T> T executeScalar(Class<T> type, String sql, Object ... args);
    default <T> List<T> toList(Class<T> type) {
        return toList(type, null);
    }
    <T> List<T> toList(Class<T> type, String selectText);
    List<Long> toIdList();
    int count();
    <T> T first(Class<T> type, String selectText);
    default <T> T first(Class<T> type) {
        return first(type, null);
    }
    <T> T firstOrDefault(Class<T> type, String selectText);
    default <T> T firstOrDefault(Class<T> type) {
        return firstOrDefault(type, null);
    }
    QueryBuilder createNew();
    <T extends DataObject> boolean deleteFrom(Class<T> type, String filter, Object ... args);
}
