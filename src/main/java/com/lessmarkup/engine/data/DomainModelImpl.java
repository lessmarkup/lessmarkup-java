package com.lessmarkup.engine.data;

import com.lessmarkup.Constants;
import com.lessmarkup.engine.data.dialects.DatabaseLanguageDialect;
import com.lessmarkup.engine.data.dialects.DatabaseLanguageDialectFactory;
import com.lessmarkup.framework.helpers.PropertyDescriptor;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.OptionalBoolean;
import com.lessmarkup.interfaces.data.QueryBuilder;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.exceptions.DatabaseException;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

class DomainModelImpl implements DomainModel {
    
    private static final HashMap<Class<?>, Integer> collectionTypeToId = new HashMap<>();
    private static final HashMap<Integer, Class<?>> collectionIdToType = new HashMap<>();
    private static final HashMap<Class<?>, TableMetadata> tableMetadatas = new HashMap<>();
    private static final HashMap<String, TableMetadata> stringToMetadata = new HashMap<>();
    private static int collectionIdCounter = 1;
    private Connection connection;
    private DatabaseLanguageDialect dialect;
    
    public DomainModelImpl() {
    }
    
    static OptionalInt getCollectionId(Class<?> collectionType) {
        Integer collectionId = collectionTypeToId.get(collectionType);
        
        if (collectionId == null) {
            return OptionalInt.empty();
        }
        
        return OptionalInt.of(collectionId);
    }
    
    public static Class<?> getCollectionType(int collectionId) {
        Class<?> collectionType = collectionIdToType.get(collectionId);
        
        if (collectionType == null) {
            throw new ArrayIndexOutOfBoundsException();
        }
        
        return collectionType;
    }
    
    static <T extends DataObject> TableMetadata getMetadata(Class<T> type) {
        return tableMetadatas.get(type);
    }
    
    static TableMetadata getMetadata(String tableName) {
        return stringToMetadata.get(tableName);
    }

    public static <T extends DataObject> void registerDataType(Class<T> collectionType) {
        collectionTypeToId.put(collectionType, collectionIdCounter);
        collectionIdToType.put(collectionIdCounter, collectionType);
        collectionIdCounter++;
        TableMetadata metadata = new TableMetadata(collectionType);
        tableMetadatas.put(collectionType, metadata);
        stringToMetadata.put(metadata.getName(), metadata);
    }
    
    @Override
    public QueryBuilder query() {
        if (this.connection == null) {
            return new QueryBuilderStubImpl();
        }
        QueryBuilderImpl ret = new QueryBuilderImpl();
        ret.setConnection(this.connection);
        return ret;
    }

    private static String getConnectionString() {
        return RequestContextHolder.getContext().getEngineConfiguration().getConnectionString();
    }
    
    void createConnectionWithTransaction() {
        String connectionString = getConnectionString();
        if (StringHelper.isNullOrEmpty(connectionString)) {
            return;
        }
        try {
            this.connection = ConnectionManager.getConnection(connectionString);
            this.connection.setAutoCommit(false);
            this.dialect = DatabaseLanguageDialectFactory.createDialect(this.connection);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } catch (ClassNotFoundException ex) {
            throw new CommonException(ex);
        }
    }
    
    void createConnection(String connectionString) {
        if (connectionString == null) {
            connectionString = getConnectionString();
        }
        if (StringHelper.isNullOrEmpty(connectionString)) {
            return;
        }
        try {
            this.connection = ConnectionManager.getConnection(connectionString);
            this.dialect = DatabaseLanguageDialectFactory.createDialect(this.connection);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } catch (ClassNotFoundException ex) {
            throw new CommonException(ex);
        }
    }

    @Override
    public void completeTransaction() {
        if (connection == null) {
            return;
        }
        try {
            this.connection.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void updateDataValue(PropertyDescriptor property, Object value, PreparedStatement statement, int columnIndex) throws SQLException {
        if (property.getType().equals(OptionalInt.class)) {
            OptionalInt valueInt = (OptionalInt) value;
            if (valueInt.isPresent()) {
                statement.setObject(columnIndex, valueInt.getAsInt());
            } else {
                statement.setNull(columnIndex, Types.INTEGER);
            }
        } else if (property.getType().equals(OptionalLong.class)) {
            OptionalLong valueLong = (OptionalLong) value;
            if (valueLong.isPresent()) {
                statement.setObject(columnIndex, valueLong.getAsLong());
            } else {
                statement.setNull(columnIndex, Types.BIGINT);
            }
        } else if (property.getType().equals(OptionalBoolean.class)) {
            OptionalBoolean valueLong = (OptionalBoolean) value;
            if (valueLong.isPresent()) {
                statement.setObject(columnIndex, valueLong.get());
            } else {
                statement.setNull(columnIndex, Types.BIT);
            }
        } else if (property.getType().equals(OptionalDouble.class)) {
            OptionalDouble valueDouble = (OptionalDouble) value;
            if (valueDouble.isPresent()) {
                statement.setObject(columnIndex, valueDouble.getAsDouble());
            } else {
                statement.setNull(columnIndex, Types.DOUBLE);
            }
        } else if (property.getType().equals(OffsetDateTime.class)) {
            if (value == null) {
                statement.setNull(columnIndex, Types.TIMESTAMP_WITH_TIMEZONE);
            } else {
                Timestamp timestamp = Timestamp.from(((OffsetDateTime)value).toInstant());
                statement.setTimestamp(columnIndex, timestamp);
            }
        } else {
            if (value == null) {
                statement.setNull(columnIndex, Types.JAVA_OBJECT);
            } else {
                statement.setObject(columnIndex, value);
            }
        }
    }

    @Override
    public <T extends DataObject> boolean update(T dataObject) {
        if (connection == null) {
            return false;
        }

        TableMetadata metadata = getMetadata(dataObject.getClass());
        
        StringBuilder command = new StringBuilder();
        command.append(String.format("UPDATE %s SET ", this.dialect.decorateName(metadata.getName())));
        
        boolean first = true;
        
        for (PropertyDescriptor column : metadata.getColumns().values()) {
            if (Constants.Data.ID_PROPERTY_NAME.equals(column.getName())) {
                continue;
            }
            
            if (!first) {
                command.append(", ");
            }
            
            first = false;
            
            command.append(String.format("%s = ?", this.dialect.decorateName(column.getName())));
        }
        
        command.append(String.format(" WHERE %s = ?", this.dialect.decorateName(Constants.Data.ID_PROPERTY_NAME)));
        
        try (PreparedStatement statement = this.connection.prepareStatement(command.toString())) {
            int columnIndex = 1;
            for (PropertyDescriptor column : metadata.getColumns().values()) {
                if (Constants.Data.ID_PROPERTY_NAME.equals(column.getName())) {
                    continue;
                }
                updateDataValue(column, column.getValue(dataObject), statement, columnIndex++);
            }
            statement.setLong(columnIndex, dataObject.getId());
            return statement.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public <T extends DataObject> boolean create(T dataObject) {
        if (connection == null) {
            return false;
        }

        TableMetadata metadata = getMetadata(dataObject.getClass());
        
        StringBuilder names = new StringBuilder();
        StringBuilder values = new StringBuilder();
        
        boolean first = true;
        
        for (PropertyDescriptor column : metadata.getColumns().values()) {
            if (Constants.Data.ID_PROPERTY_NAME.equals(column.getName())) {
                continue;
            }
            
            if (first) {
                first = false;
            } else {
                names.append(", ");
                values.append(", ");
            }
            
            names.append(this.dialect.decorateName(column.getName()));
            values.append("?");
        }

        String command = String.format("INSERT INTO %s (%s) VALUES (%s)", this.dialect.decorateName(metadata.getName()), names.toString(), values.toString());
        
        try (PreparedStatement statement = this.connection.prepareStatement(command, Statement.RETURN_GENERATED_KEYS)) {
            int columnIndex = 1;
            for (PropertyDescriptor column : metadata.getColumns().values()) {
                if (Constants.Data.ID_PROPERTY_NAME.equals(column.getName())) {
                    continue;
                }
                updateDataValue(column, column.getValue(dataObject), statement, columnIndex++);
            }
            
            int createdRecords = statement.executeUpdate();
            if (createdRecords == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    return false;
                }
                
                dataObject.setId(generatedKeys.getLong(1));
            }
            
            return true;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public <T extends DataObject> boolean delete(Class<T> type, long id) {
        if (connection == null) {
            return false;
        }

        TableMetadata metadata = getMetadata(type);
        
        try (Statement statement = this.connection.createStatement()) {
            return statement.execute(String.format("DELETE FROM %s WHERE %s=%d", this.dialect.decorateName(metadata.getName()), this.dialect.decorateName(Constants.Data.ID_PROPERTY_NAME), id));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void close() {
        if (connection == null) {
            return;
        }
        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
