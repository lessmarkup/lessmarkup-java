package com.lessmarkup.interfaces.data;

import java.util.OptionalInt;

public interface DomainModelProvider {
    DomainModel create();
    DomainModel create(String connectionString);
    DomainModel createWithTransaction();
    OptionalInt getCollectionId(Class<?> collectionType);
    void initialize();
}
