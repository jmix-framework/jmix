/*
 * Copyright 2020 Haulmont.
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

package io.jmix.eclipselink.impl.mapping;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.eclipselink.impl.UuidConverter;
import io.jmix.eclipselink.impl.dbms.UuidMappingInfo;
import io.jmix.eclipselink.persistence.MappingProcessor;
import io.jmix.eclipselink.persistence.MappingProcessorContext;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.platform.database.*;
import org.eclipse.persistence.sessions.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.UUID;

/**
 * Updates entity mappings to add UUID support even for databases that do not support UUID datatype directly.
 */
@Component("eclipselink_UuidMappingProcessor")
public class UuidMappingProcessor implements MappingProcessor {
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;

    @Override
    public void process(MappingProcessorContext context) {
        DatabaseMapping mapping = context.getMapping();

        MetaClass metaClass = metadata.getClass(mapping.getDescriptor().getJavaClass());
        MetaProperty metaProperty = metaClass.getProperty(mapping.getAttributeName());

        if (metaProperty.getRange().isDatatype()) {
            if (metaProperty.getJavaType().equals(UUID.class)) {
                if (context.getSession().getPlatform() instanceof UuidMappingInfo) {
                    UuidMappingInfo mappingInfo = (UuidMappingInfo) context.getSession().getPlatform();
                    ((DirectToFieldMapping) mapping).setConverter(mappingInfo.getUuidConverter());
                    setFieldProperties(mappingInfo, mapping.getField());
                } else {
                    ((DirectToFieldMapping) mapping).setConverter(UuidConverter.getInstance());
                    setDatabaseFieldParameters(context.getSession(), mapping.getField());
                }
            }
        } else if (metaProperty.getRange().isClass() && !metaProperty.getRange().getCardinality().isMany()) {
            MetaProperty refPkProperty = metadataTools.getPrimaryKeyProperty(metaProperty.getRange().asClass());
            if (refPkProperty != null && refPkProperty.getJavaType().equals(UUID.class)) {
                for (DatabaseField field : ((OneToOneMapping) mapping).getForeignKeyFields()) {
                    if (context.getSession().getPlatform() instanceof UuidMappingInfo) {
                        setFieldProperties((UuidMappingInfo) context.getSession().getPlatform(), field);
                    } else {
                        setDatabaseFieldParameters(context.getSession(), field);
                    }
                }
            }
        }
    }

    private void setFieldProperties(UuidMappingInfo mappingInfo, DatabaseField field) {
        field.setSqlType(mappingInfo.getUuidSqlType());
        field.setType(mappingInfo.getUuidType());
        field.setColumnDefinition(mappingInfo.getUuidColumnDefinition());
    }

    @Deprecated
    private void setDatabaseFieldParameters(Session session, DatabaseField field) {
        if (session.getPlatform() instanceof PostgreSQLPlatform) {
            field.setSqlType(Types.OTHER);
            field.setType(UUID.class);
            field.setColumnDefinition("UUID");
        } else if (session.getPlatform() instanceof MySQLPlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("varchar(32)");
        } else if (session.getPlatform() instanceof HSQLPlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("varchar(36)");
        } else if (session.getPlatform() instanceof SQLServerPlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("uniqueidentifier");
        } else if (session.getPlatform() instanceof OraclePlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("varchar2(32)");
        } else {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
        }
    }
}
