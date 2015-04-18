package com.lessmarkup.interfaces.data;

public interface DomainModelProvider {
    DomainModel create();
    DomainModel create(String connectionString);
    DomainModel createWithTransaction();
    int getCollectionId(Class<?> collectionType);
    void initialize();
}
