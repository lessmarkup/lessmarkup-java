/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lessmarkup.engine.data.migrate;

import com.lessmarkup.Constants;
import com.lessmarkup.engine.data.ConnectionManager;
import com.lessmarkup.engine.data.dialects.DatabaseLanguageDialect;
import com.lessmarkup.engine.data.dialects.DatabaseDataType;
import com.lessmarkup.engine.data.dialects.DatabaseLanguageDialectFactory;
import com.lessmarkup.framework.helpers.LoggingHelper;
import com.lessmarkup.framework.helpers.PropertyDescriptor;
import com.lessmarkup.framework.helpers.StringHelper;
import com.lessmarkup.framework.helpers.TypeHelper;
import com.lessmarkup.interfaces.data.DataObject;
import com.lessmarkup.interfaces.data.Migrator;
import com.lessmarkup.interfaces.data.OptionalBoolean;
import com.lessmarkup.interfaces.exceptions.CommonException;
import com.lessmarkup.interfaces.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.*;

import com.lessmarkup.interfaces.recordmodel.MaxLength;
import com.lessmarkup.interfaces.recordmodel.RequiredField;
import org.atteo.evo.inflector.English;

public class MigratorImpl implements Migrator {

    private final Connection connection;
    private final DatabaseLanguageDialect dialect;
    
    public MigratorImpl(String connectionString) {
        if (StringHelper.isNullOrEmpty(connectionString)) {
            this.connection = null;
            this.dialect = null;
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
    public void executeSql(String sql) {
        if (connection == null) {
            return;
        }
        try (Statement statement = this.connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
    
    public Object executeScalar(String sql) {
        if (connection == null) {
            return null;
        }
        try (Statement statement = this.connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.first()) {
                return null;
            }
            return resultSet.getObject(1);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
    
    @Override
    public <T extends DataObject> boolean checkExists(Class<T> type) {
        if (connection == null) {
            return true;
        }
        String tableName = English.plural(type.getSimpleName());
        return (long) executeScalar(String.format("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '%s'", tableName)) > 0;
    }
    
    private String getDataType(PropertyDescriptor property) {

        Class<?> propertyType = property.getType();
        
        if (propertyType.equals(OptionalLong.class)) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.LONG(), false);
        }
        
        if (propertyType.equals(OptionalInt.class)) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.INT(), false);
        }

        if (propertyType.equals(OptionalBoolean.class)) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.BOOLEAN(), false);
        }

        if (propertyType.equals(OptionalDouble.class)) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.DOUBLE(), false);
        }

        boolean required = property.getAnnotation(RequiredField.class) != null;
        OptionalInt maxLength = OptionalInt.empty();

        MaxLength maxLengthAnnotation = property.getAnnotation(MaxLength.class);
        if (maxLengthAnnotation != null) {
            maxLength = OptionalInt.of(maxLengthAnnotation.length());
        }

        if (propertyType.equals(int.class) || propertyType.equals(Integer.class)) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.INT(), required);
        }
        
        if (propertyType.equals(OffsetDateTime.class)) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.DATE_TIME(), required);
        }

        if (propertyType.equals(long.class) || propertyType.equals(Long.class)) {
            if (property.getName().equals(Constants.Data.ID_PROPERTY_NAME)) {
                return this.dialect.getTypeDeclaration(DatabaseDataType.IDENTITY(), required);
            }
            return this.dialect.getTypeDeclaration(DatabaseDataType.LONG(), required);
        }

        if (propertyType.equals(boolean.class) || propertyType.equals(Boolean.class)) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.BOOLEAN(), required);
        }

        if (propertyType.equals(double.class) || propertyType.equals(Double.class)) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.DOUBLE(), required);
        }

        if (propertyType.equals(String.class)) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.STRING(), maxLength, required, null);
        }

        if (propertyType.equals(byte[].class)) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.BINARY(), maxLength, required, null);
        }

        if (propertyType.isEnum()) {
            return this.dialect.getTypeDeclaration(DatabaseDataType.INT(), required);
        }

        throw new IllegalArgumentException();
    }

    @Override
    public <T extends DataObject> void createTable(Class<T> type) {
        if (checkExists(type)) {
            updateTable(type);
            return;
        }

        String tableName = English.plural(type.getSimpleName());

        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("CREATE TABLE %s (", this.dialect.decorateName(tableName)));

        boolean first = true;
        
        for (PropertyDescriptor property : TypeHelper.getProperties(type)) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            String dataType = getDataType(property);
            sb.append(String.format("%s %s", this.dialect.decorateName(property.getName()), dataType));
        }
        
        sb.append(String.format(", CONSTRAINT %s PRIMARY KEY (%s ASC))", this.dialect.decorateName("PK_" + tableName), this.dialect.decorateName(Constants.Data.ID_PROPERTY_NAME)));

        try {
            executeSql(sb.toString());
        } catch (Exception e) {
            LoggingHelper.getLogger(getClass()).info("Error creating table: " + sb.toString());
            throw e;
        }
    }

    <TD extends DataObject, TB extends DataObject> boolean checkDependency(Class<TD> typeD, Class<TB> typeB, String column) {
        if (connection == null) {
            return true;
        }
        
        String dependentTableName = English.plural(typeD.getSimpleName());
        String baseTableName = English.plural(typeB.getSimpleName());

        if (column == null) {
            column = String.format("%sId", typeB.getSimpleName());
        }

        String text = String.format("SELECT COUNT(*) FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_NAME = 'FK_%s_%s_%s'", dependentTableName, baseTableName, column);

        return (long) executeScalar(text) > 0;
    }
    
    @Override
    public <TD extends DataObject, TB extends DataObject> void addDependency(Class<TD> typeD, Class<TB> typeB, String column) {
        if (connection == null) {
            return;
        }
        
        if (checkDependency(typeD, typeB, column)) {
            return;
        }

        String dependentTableName = English.plural(typeD.getSimpleName());
        String baseTableName = English.plural(typeB.getSimpleName());

        if (column == null) {
            column = String.format("%sId", typeB.getSimpleName());
        }

        String text = String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY(%s) REFERENCES %s (%s)",
                this.dialect.decorateName(dependentTableName),
                this.dialect.decorateName(String.format("FK_%s_%s_%s", dependentTableName, baseTableName, column)),
                this.dialect.decorateName(column),
                this.dialect.decorateName(baseTableName),
                this.dialect.decorateName(Constants.Data.ID_PROPERTY_NAME));

        executeSql(text);

        text = String.format("CREATE INDEX %s ON %S (%s ASC)",
                this.dialect.decorateName("IX_" + column),
                this.dialect.decorateName(dependentTableName),
                this.dialect.decorateName(column));

        executeSql(text);
    }

    @Override
    public <TD extends DataObject, TB extends DataObject> void deleteDependency(Class<TD> typeD, Class<TB> typeB, String column) {
        if (connection == null) {
            return;
        }

        final String dependentTableName = English.plural(typeD.getSimpleName());
        final String baseTableName = English.plural(typeB.getSimpleName());

        if (column == null) {
            column = String.format("%sId", typeB.getSimpleName());
        }

        String text = String.format("DROP INDEX %s ON %s", this.dialect.decorateName("IX_" + column), this.dialect.decorateName(dependentTableName));

        executeSql(text);

        text = String.format("ALTER TABLE %s DROP CONSTRAINT %s",
                this.dialect.decorateName(dependentTableName),
                this.dialect.decorateName(String.format("FK_%s_%s_%s", dependentTableName, baseTableName, column)));

        executeSql(text);
    }

    @Override
    public <T extends DataObject> void updateTable(Class<T> type) {
        if (connection == null) {
            return;
        }

        final String tableName = English.plural(type.getSimpleName());
        
        final Map<String, String> columnsToAdd = new HashMap<>();
        
        final String columnId = Constants.Data.ID_PROPERTY_NAME.toLowerCase();
        
        for (PropertyDescriptor property : TypeHelper.getProperties(type)) {
            String columnName = property.getName().toLowerCase();
            if (columnName.equals(columnId)) {
                continue;
            }
            columnsToAdd.put(columnName, getDataType(property));
        }

        List<String> columnsToDrop = new ArrayList<>();

        try (Statement statement = this.connection.createStatement()) {
            try (ResultSet result = statement.executeQuery(
                    "SELECT COLUMN_NAME, IS_NULLABLE, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, CHARACTER_SET_NAME " + 
                    "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "'")) {
                while (result.next()) {
                    String columnName = result.getString(1);
                    String columnNameLower = columnName.toLowerCase();

                    if (columnId.equals(columnNameLower)) {
                        continue;
                    }

                    final String currentType = columnsToAdd.get(columnNameLower);
                    if (currentType == null) {
                        columnsToDrop.add(columnName);
                        continue;
                    }

                    final boolean required = !"YES".equals(result.getString(2));
                    final DatabaseDataType dataType = this.dialect.getDataType(result.getString(3));
                    if (dataType == null) {
                        columnsToDrop.add(columnName);
                        continue;
                    }
                    OptionalInt sizeLimit = OptionalInt.empty();
                    int value = result.getInt(4);
                    if (!result.wasNull() && value != 65535) {
                        sizeLimit = OptionalInt.of(value);
                    }
                    
                    String characterSet = result.getString(5);
                    if (result.wasNull()) {
                        characterSet = null;
                    }

                    final String columnType = this.dialect.getTypeDeclaration(dataType, sizeLimit, required, characterSet);

                    if (!currentType.equals(columnType)) {
                        columnsToDrop.add(columnName);
                    }
                    else {
                        columnsToAdd.remove(columnNameLower);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        
        for (String column : columnsToDrop) {
            executeSql(String.format("ALTER TABLE %s DROP COLUMN %s", this.dialect.decorateName(tableName), this.dialect.decorateName(column)));
        }
        
        for (Map.Entry<String, String> entry : columnsToAdd.entrySet()) {
            executeSql(String.format("ALTER TABLE %s ADD %s %s", this.dialect.decorateName(tableName), this.dialect.decorateName(entry.getKey()), entry.getValue()));
        }
    }

    @Override
    public <T extends DataObject> void deleteTable(Class<T> type) {
        if (connection == null) {
            return;
        }
        String text = String.format("DROP TABLE %s", this.dialect.decorateName(English.plural(type.getSimpleName())));
        executeSql(text);
    }
}
