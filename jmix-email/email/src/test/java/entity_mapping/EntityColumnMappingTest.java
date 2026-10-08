/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package entity_mapping;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.email.entity.RefreshToken;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.EmailTestConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the length limits of string attributes, which UI components take from the entity metadata,
 * match the database columns created by the add-on Liquibase changelog.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EmailTestConfiguration.class})
public class EntityColumnMappingTest {

    @Autowired
    Metadata metadata;

    @Autowired
    MetadataTools metadataTools;

    @Autowired
    DataSource dataSource;

    @Test
    void testStringAttributesMatchDatabaseColumns() throws SQLException {
        List<String> mismatches = new ArrayList<>();

        for (Class<?> entityClass : List.of(SendingMessage.class, SendingAttachment.class, RefreshToken.class)) {
            MetaClass metaClass = metadata.getClass(entityClass);
            Map<String, ColumnInfo> columns = loadColumns(metadataTools.getDatabaseTable(metaClass));

            for (MetaProperty property : metaClass.getProperties()) {
                if (!isStringAttribute(property)) {
                    continue;
                }

                String attribute = metaClass.getName() + "." + property.getName();
                ColumnInfo column = columns.get(metadataTools.getDatabaseColumn(property));
                Object length = property.getAnnotations().get(MetadataTools.LENGTH_ANN_NAME);

                if (column == null) {
                    mismatches.add(attribute + ": column not found");
                } else if (column.dataType() == Types.CLOB) {
                    if (!metadataTools.isLob(property)) {
                        mismatches.add(attribute + ": CLOB column, but the attribute is not a LOB (length " + length + ")");
                    }
                } else if (!Integer.valueOf(column.size()).equals(length)) {
                    mismatches.add(attribute + ": column length " + column.size() + ", attribute length " + length);
                }
            }
        }

        assertTrue(mismatches.isEmpty(),
                () -> "Attributes do not match database columns:\n" + String.join("\n", mismatches));
    }

    private boolean isStringAttribute(MetaProperty property) {
        return property.getRange().isDatatype()
                && property.getRange().asDatatype().getJavaClass() == String.class
                && metadataTools.getDatabaseColumn(property) != null;
    }

    private Map<String, ColumnInfo> loadColumns(String table) throws SQLException {
        Map<String, ColumnInfo> columns = new HashMap<>();
        try (Connection connection = dataSource.getConnection();
             ResultSet resultSet = connection.getMetaData().getColumns(null, null, table, null)) {
            while (resultSet.next()) {
                columns.put(resultSet.getString("COLUMN_NAME"),
                        new ColumnInfo(resultSet.getInt("DATA_TYPE"), resultSet.getInt("COLUMN_SIZE")));
            }
        }
        return columns;
    }

    private record ColumnInfo(int dataType, int size) {
    }
}
