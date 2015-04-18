package com.lessmarkup.engine.data;

import com.lessmarkup.Constants;
import com.lessmarkup.engine.data.dialects.DatabaseLanguageDialect;
import com.lessmarkup.engine.data.dialects.DatabaseLanguageDialectFactory;
import com.lessmarkup.framework.helpers.DependencyResolver;
import com.lessmarkup.framework.helpers.PropertyDescriptor;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.helpers.TypeHelper;
import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.data.OptionalBoolean;
import com.lessmarkup.interfaces.data.QueryBuilder;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

class QueryBuilderImpl implements QueryBuilder {
    
    private Connection connection;
    private DatabaseLanguageDialect dialect;
    
    private final StringBuilder commandText = new StringBuilder();
    private String select = "*";
    private final StringBuilder where = new StringBuilder();
    private final StringBuilder orderBy = new StringBuilder();
    private String limit = "";
    private final List<Object> parameters = new ArrayList<>();
    
    void setConnection(Connection connection) {
        this.connection = connection;
        try {
            this.dialect = DatabaseLanguageDialectFactory.createDialect(this.connection);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public <T extends DataObject> QueryBuilder from(Class<T> type, String name) {
        TableMetadata metadata = DomainModelImpl.getMetadata(type);
        if (metadata == null) {
            throw new IllegalArgumentException();
        }
        this.commandText.append(String.format(" FROM %s %s", this.dialect.decorateName(metadata.getName()), name != null ? name : ""));
        return this;
    }
    
    private <T extends DataObject> QueryBuilder anyJoin(Class<T> type, String joinType, String name, String on) {
        TableMetadata metadata = DomainModelImpl.getMetadata(type);
        if (metadata == null) {
            throw new IllegalArgumentException();
        }
        this.commandText.append(String.format(" %sJOIN %s %s ON %s", joinType, this.dialect.decorateName(metadata.getName()), name, on));
        return this;
    }
    
    private String processStringWithParameters(String sql, Object[] args) {
        if (args.length == 0) {
            return sql;
        }
        
        int pos = 0;
        int argIndex = 0;
        
        StringBuilder ret = new StringBuilder();
        
        for (;;) {
            int newPos = sql.indexOf('$', pos);
            
            if (newPos < 0) {
                ret.append(sql.substring(pos));
                break;
            }
            
            if (newPos > pos) {
                ret.append(sql.substring(pos, newPos));
            }
            
            pos = newPos;
            
            boolean isTableName = pos + 1 < sql.length() && sql.charAt(pos+1) == '-';
            
            if (isTableName) {
                int pos1 = pos+2;
                for (;pos1 < sql.length() && Character.isAlphabetic(sql.charAt(pos1)); pos1++) {
                }
                String tableName = sql.substring(pos+2, pos1);
                tableName = DomainModelImpl.getMetadata(tableName).getName();
                ret.append(tableName);
                pos = pos1;
                continue;
            }
            
            if (pos + 1 < sql.length() && Character.isDigit(sql.charAt(pos+1))) {
                int start = pos+1;
                pos += 2;
                for (; pos < sql.length() && Character.isDigit(sql.charAt(pos)); pos++) {
                }
                int parameterIndex = Integer.decode(sql.substring(start, pos));
                ret.append('?');
                this.parameters.add(args[parameterIndex]);
                continue;
            }
            
            ret.append('?');
            this.parameters.add(args[argIndex++]);
            pos++;
        }
        
        return ret.toString();
    }

    @Override
    public <T extends DataObject> QueryBuilder join(Class<T> type, String name, String on) {
        return anyJoin(type, "", name, on);
    }

    @Override
    public <T extends DataObject> QueryBuilder leftJoin(Class<T> type, String name, String on) {
        return anyJoin(type, "LEFT ", name, on);
    }

    @Override
    public <T extends DataObject> QueryBuilder rightJoin(Class<T> type, String name, String on) {
        return anyJoin(type, "RIGHT ", name, on);
    }

    @Override
    public QueryBuilder where(String filter, Object... args) {
        filter = processStringWithParameters(filter, args);
        
        if (this.where.length() > 0) {
            this.where.append(" AND ");
        }
        this.where.append(filter);
        
        return this;
    }

    @Override
    public QueryBuilder whereIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            throw new IllegalArgumentException();
        }
        List<String> idsText = new ArrayList<>();
        ids.stream().map(id -> id.toString()).forEach(idsText::add);
        if (this.where.length() > 0) {
            this.where.append(" AND ");
        }
        this.where.append(String.format("%s IN (%s)", this.dialect.decorateName(Constants.Data.ID_PROPERTY_NAME), String.join(",", idsText)));
        return this;
    }

    @Override
    public QueryBuilder orderBy(String column) {
        if (this.orderBy.length() > 0) {
        this.orderBy.append(String.format(", %s", this.dialect.decorateName(column)));
        } else {
            this.orderBy.append(String.format("ORDER BY %s", this.dialect.decorateName(column)));
        }
        return this;
    }

    @Override
    public QueryBuilder orderByDescending(String column) {
        if (this.orderBy.length() > 0) {
        this.orderBy.append(String.format(", %s DESC", this.dialect.decorateName(column)));
        } else {
            this.orderBy.append(String.format("ORDER BY %s DESC", this.dialect.decorateName(column)));
        }
        return this;
    }

    @Override
    public QueryBuilder groupBy(String column) {
        this.commandText.append(String.format(" GROUP BY %s ", this.dialect.decorateName(column)));
        return this;
    }

    @Override
    public QueryBuilder limit(int from, int count) {
        this.limit = this.dialect.paging(from, count);
        return this;
    }

    @Override
    public <T extends DataObject> T find(Class<T> type, long id) {
        if (commandText.length() == 0) {
            from(type);
        }
        
        return where(this.dialect.decorateName(Constants.Data.ID_PROPERTY_NAME) + " = $", id).first(type);
    }

    @Override
    public <T extends DataObject> T findOrDefault(Class<T> type, long id) {
        if (commandText.length() == 0) {
            from(type);
        }
        
        return where(this.dialect.decorateName(Constants.Data.ID_PROPERTY_NAME) + " = $", id).firstOrDefault(type);
    }
    
    private String getSql() {
        StringBuilder ret = new StringBuilder();
        ret.append("SELECT ").append(this.select).append(" ").append(this.commandText);
        
        if (this.where.length() > 0) {
            ret.append(" WHERE ").append(this.where);
        }
        
        if (this.orderBy.length() > 0) {
            ret.append(" ").append(this.orderBy);
        }
        
        if (this.limit.length() > 0) {
            ret.append(" ").append(this.limit);
        }
        
        return ret.toString();
    }
    
    private PreparedStatement prepareStatement(String sql) {
        try {
            PreparedStatement localStatement = this.connection.prepareStatement(sql);
            int parameterIndex = 1;
            for (Object param : this.parameters) {
                localStatement.setObject(parameterIndex++, param);
            }
            return localStatement;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
    
    private void readDataValue(PropertyDescriptor property, Object dataObject, ResultSet resultSet, int index) throws SQLException {
        if (property.getType().equals(OptionalInt.class)) {
            OptionalInt intValue = OptionalInt.of(resultSet.getInt(index));
            if (resultSet.wasNull()) {
                intValue = OptionalInt.empty();
            }
            property.setValue(dataObject, intValue);
        } else if (property.getType().equals(OptionalLong.class)) {
            OptionalLong longValue = OptionalLong.of(resultSet.getLong(index));
            if (resultSet.wasNull()) {
                longValue = OptionalLong.empty();
            }
            property.setValue(dataObject, longValue);
        } else if (property.getType().equals(OptionalDouble.class)) {
            OptionalDouble doubleValue = OptionalDouble.of(resultSet.getDouble(index));
            if (resultSet.wasNull()) {
                doubleValue = OptionalDouble.empty();
            }
            property.setValue(dataObject, doubleValue);
        } else if(property.getType().equals(OptionalBoolean.class)) {
            OptionalBoolean booleanValue = OptionalBoolean.of(resultSet.getBoolean(index));
            if (resultSet.wasNull()) {
                booleanValue = OptionalBoolean.empty();
            }
            property.setValue(dataObject, booleanValue);
        } else if (property.getType().equals(OffsetDateTime.class)) {
            Timestamp timestamp = resultSet.getTimestamp(index);
            if (resultSet.wasNull()) {
                property.setValue(dataObject, null);
            } else {
                OffsetDateTime dateTime = OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC);
                property.setValue(dataObject, dateTime);
            }
        } else {
            Object value = resultSet.getObject(index);
            if (resultSet.wasNull()) {
                property.setValue(dataObject, null);
            } else {
                property.setValue(dataObject, value);
            }
        }
    }

    private <T extends DataObject> List<T> executeOnDataObjectWithLimit(Class<T> type, String sql, OptionalInt limit, Object ... args) {
        this.parameters.clear();
        sql = processStringWithParameters(sql, args);
        List<T> ret = new ArrayList<>();
        TableMetadata metadata = DomainModelImpl.getMetadata(type);
        try (PreparedStatement statement = prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            
            ResultSetMetaData resultSetMetadata = resultSet.getMetaData();
            while (resultSet.next()) {
                
                T dataObject;
                try {
                    dataObject = type.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    throw new CommonException(ex);
                }
                
                for (int i = 1; i <= resultSetMetadata.getColumnCount(); i++) {
                    String columnName = resultSetMetadata.getColumnName(i);
                    PropertyDescriptor column = metadata.getColumns().get(columnName);
                    if (column == null) {
                        continue;
                    }
                    readDataValue(column, dataObject, resultSet, i);
                }

                ret.add(dataObject);

                if (limit.isPresent() && ret.size() == limit.getAsInt()) {
                    break;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return ret;
    }
    
    private <T> List<T> executeOnRegularObjectWithLimit(Class<T> type, String sql, OptionalInt limit, Object ... args) {
        sql = processStringWithParameters(sql, args);
        List<T> ret = new ArrayList<>();
        
        boolean isDataObject = DataObject.class.isAssignableFrom(type);
        
        Map<String, PropertyDescriptor> properties = new HashMap<>();
        
        for (PropertyDescriptor property : TypeHelper.getProperties(type)) {
            properties.put(property.getName(), property);
        }

        try (
                PreparedStatement statement = prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            
            ResultSetMetaData resultSetMetadata = resultSet.getMetaData();
            while (resultSet.next()) {
                
                T dataObject;
                if (isDataObject) {
                    try {
                        dataObject = type.newInstance();
                    } catch (InstantiationException | IllegalAccessException ex) {
                        throw new CommonException(ex);
                    }
                } else {
                    dataObject = DependencyResolver.resolve(type);
                }
                
                for (int i = 1; i <= resultSetMetadata.getColumnCount(); i++) {
                    String columnName = resultSetMetadata.getColumnName(i);
                    PropertyDescriptor property = properties.get(StringHelper.toJsonCase(columnName));
                    if (property == null) {
                        continue;
                    }
                    readDataValue(property, dataObject, resultSet, i);
                }

                ret.add(dataObject);

                if (limit.isPresent() && limit.getAsInt() == ret.size()) {
                    break;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return ret;
    }
    
    @Override
    public <T extends DataObject> List<T> execute(Class<T> type, String sql, Object... args) {
        return executeOnDataObjectWithLimit(type, sql, OptionalInt.empty(), args);
    }

    @Override
    public boolean executeNonQuery(String sql, Object... args) {
        try (PreparedStatement localStatement = prepareStatement(sql)) {
            return localStatement.execute();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public <T> T executeScalar(Class<T> type, String sql, Object... args) {
        try (PreparedStatement localStatement = prepareStatement(sql)) {
            try (ResultSet resultSet = localStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getObject(1, type);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public <T> List<T> toList(Class<T> type, String selectText) {
        
        if (this.select.length() == 0) {
            this.select = selectText;
        }
        
        return executeOnRegularObjectWithLimit(type, getSql(), OptionalInt.empty());
    }

    @Override
    public List<Long> toIdList() {
        List<Long> ret = new ArrayList<>();
        try (PreparedStatement statement = prepareStatement(getSql())) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ret.add(resultSet.getLong(Constants.Data.ID_PROPERTY_NAME));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return ret;
    }

    @Override
    public int count() {
        this.select = "COUNT(*)";
        return (int)(long) executeScalar(Integer.class, getSql());
    }

    @Override
    public <T> T first(Class<T> type, String selectText) {
        if (selectText != null && selectText.length() > 0) {
            this.select = selectText;
        }
        
        List<T> ret = executeOnRegularObjectWithLimit(type, getSql(), OptionalInt.of(1));
        
        if (ret.isEmpty()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        
        return ret.get(0);
    }

    @Override
    public <T> T firstOrDefault(Class<T> type, String selectText) {
        if (selectText != null && selectText.length() > 0) {
            this.select = selectText;
        }
        
        List<T> ret = executeOnRegularObjectWithLimit(type, getSql(), OptionalInt.of(1));
        
        if (ret.isEmpty()) {
            return null;
        }
        
        return ret.get(0);
    }

    @Override
    public QueryBuilder createNew() {
        QueryBuilderImpl ret = new QueryBuilderImpl();
        ret.connection = this.connection;
        return ret;
    }

    @Override
    public <T extends DataObject> boolean deleteFrom(Class<T> type, String filter, Object... args) {
        filter = processStringWithParameters(filter, args);
        TableMetadata metadata = DomainModelImpl.getMetadata(type);
        if (metadata == null) {
            throw new IllegalArgumentException();
        }
        String sql = String.format("DELETE FROM %s WHERE %s", this.dialect.decorateName(metadata.getName()), filter);
        try (PreparedStatement statement = prepareStatement(sql)) {
            return statement.execute();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
