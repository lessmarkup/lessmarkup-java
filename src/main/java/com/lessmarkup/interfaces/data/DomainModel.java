package com.lessmarkup.interfaces.data;

public interface DomainModel extends AutoCloseable {
    QueryBuilder query();
    void completeTransaction();
    <T extends DataObject> boolean update(T dataObject);
    <T extends DataObject> boolean create(T dataObject);
    <T extends DataObject> boolean delete(Class<T> type, long id);
    @Override
    void close();
}
