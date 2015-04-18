package com.lessmarkup.interfaces.recordmodel;

import com.lessmarkup.interfaces.data.DomainModel;
import java.util.List;

public interface PropertyCollectionManager {
    List<String> getCollection(DomainModel domainModel, String property, String searchText);
}
