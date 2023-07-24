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

import com.google.common.base.Preconditions;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.eclipselink.impl.dbms.UuidMappingInfo;
import io.jmix.eclipselink.persistence.MappingProcessor;
import io.jmix.eclipselink.persistence.MappingProcessorContext;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.platform.database.DatabasePlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        DatabasePlatform platform = context.getSession().getPlatform();
        Preconditions.checkState(platform instanceof UuidMappingInfo,
                "Database platform '%s' doesn't implement %s",
                platform.getClass().getSimpleName(),
                UuidMappingInfo.class.getSimpleName());

        UuidMappingInfo mappingInfo = (UuidMappingInfo) platform;
        DatabaseMapping mapping = context.getMapping();

        MetaClass metaClass = metadata.getClass(mapping.getDescriptor().getJavaClass());
        MetaProperty metaProperty = metaClass.getProperty(mapping.getAttributeName());

        if (metaProperty.getRange().isDatatype()) {
            if (metaProperty.getJavaType().equals(UUID.class)) {
                ((DirectToFieldMapping) mapping).setConverter(mappingInfo.getUuidConverter());
                setFieldProperties(mappingInfo, mapping.getField());
            }
        } else if (metaProperty.getRange().isClass() && !metaProperty.getRange().getCardinality().isMany()) {
            MetaProperty refPkProperty = metadataTools.getPrimaryKeyProperty(metaProperty.getRange().asClass());
            if (refPkProperty != null && refPkProperty.getJavaType().equals(UUID.class)) {
                for (DatabaseField field : ((OneToOneMapping) mapping).getForeignKeyFields()) {
                    setFieldProperties(mappingInfo, field);
                }
            }
        }
    }

    private void setFieldProperties(UuidMappingInfo mappingInfo, DatabaseField field) {
        field.setSqlType(mappingInfo.getUuidSqlType());
        field.setType(mappingInfo.getUuidType());
        field.setColumnDefinition(mappingInfo.getUuidColumnDefinition());
    }
}
