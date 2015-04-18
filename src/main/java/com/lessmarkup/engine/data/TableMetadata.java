package com.lessmarkup.engine.data;

import com.lessmarkup.framework.helpers.PropertyDescriptor;
import com.lessmarkup.framework.helpers.TypeHelper;
import java.util.HashMap;
import org.atteo.evo.inflector.English;

class TableMetadata {
    
    private final String name;
    private final HashMap<String, PropertyDescriptor> columns;
    
    public TableMetadata(Class<?> sourceType) {
        this.name = English.plural(sourceType.getSimpleName());
        this.columns = new HashMap<>();
        
        for (PropertyDescriptor property : TypeHelper.getProperties(sourceType)) {
            this.columns.put(property.getName(), property);
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public HashMap<String, PropertyDescriptor> getColumns() {
        return this.columns;
    }
}
