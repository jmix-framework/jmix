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

package io.jmix.search.index.mapping.strategy.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.FileRefDatatype;
import io.jmix.core.metamodel.datatype.impl.StringDatatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.mapping.FieldConfiguration;
import io.jmix.search.index.mapping.fieldmapper.FieldMapper;
import io.jmix.search.index.mapping.fieldmapper.FieldMapperProvider;
import io.jmix.search.index.mapping.fieldmapper.impl.EnumFieldMapper;
import io.jmix.search.index.mapping.fieldmapper.impl.FileFieldMapper;
import io.jmix.search.index.mapping.fieldmapper.impl.ReferenceFieldMapper;
import io.jmix.search.index.mapping.fieldmapper.impl.TextFieldMapper;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractor;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractorProvider;
import io.jmix.search.index.mapping.propertyvalue.impl.EnumPropertyValueExtractor;
import io.jmix.search.index.mapping.propertyvalue.impl.FilePropertyValueExtractor;
import io.jmix.search.index.mapping.propertyvalue.impl.ReferencePropertyValueExtractor;
import io.jmix.search.index.mapping.propertyvalue.impl.SimplePropertyValueExtractor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Strategy that automatically maps properties the most common way.
 */
@Component("search_AutoMappingStrategy")
public class AutoMappingStrategy implements FieldMappingStrategy {

    protected final PropertyValueExtractorProvider propertyValueExtractorProvider;
    protected final FieldMapperProvider fieldMapperProvider;

    @Autowired
    public AutoMappingStrategy(PropertyValueExtractorProvider propertyValueExtractorProvider,
                               FieldMapperProvider fieldMapperProvider) {
        this.propertyValueExtractorProvider = propertyValueExtractorProvider;
        this.fieldMapperProvider = fieldMapperProvider;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isSupported(MetaPropertyPath propertyPath) {
        return hasFieldMapper(propertyPath) && hasPropertyValueExtractor(propertyPath);
    }

    @Override
    public FieldConfiguration createFieldConfiguration(MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        FieldMapper fieldMapper = resolveFieldMapper(propertyPath)
                .orElseThrow(() -> new RuntimeException("Property '" + propertyPath + "' is not supported"));
        ObjectNode jsonConfig = fieldMapper.createJsonConfiguration(parameters);
        return FieldConfiguration.create(jsonConfig);
    }

    @Override
    public PropertyValueExtractor getPropertyValueExtractor(MetaPropertyPath propertyPath) {
        return resolvePropertyValueExtractor(propertyPath)
                .orElseThrow(() -> new RuntimeException("Property '" + propertyPath + "' is not supported"));
    }

    protected boolean hasFieldMapper(MetaPropertyPath propertyPath) {
        return resolveFieldMapper(propertyPath).isPresent();
    }

    protected boolean hasPropertyValueExtractor(MetaPropertyPath propertyPath) {
        return resolvePropertyValueExtractor(propertyPath).isPresent();
    }

    protected Optional<FieldMapper> resolveFieldMapper(MetaPropertyPath propertyPath) {
        FieldMapper fieldMapper = null;
        if (propertyPath.getRange().isDatatype()) {
            Datatype<?> datatype = propertyPath.getRange().asDatatype();
            if (datatype instanceof StringDatatype) {
                fieldMapper = fieldMapperProvider.getFieldMapper(TextFieldMapper.class);
            } else if (datatype instanceof FileRefDatatype) {
                fieldMapper = fieldMapperProvider.getFieldMapper(FileFieldMapper.class);
            }
        } else if (propertyPath.getRange().isClass()) {
            fieldMapper = fieldMapperProvider.getFieldMapper(ReferenceFieldMapper.class);
        } else if (propertyPath.getRange().isEnum()) {
            fieldMapper = fieldMapperProvider.getFieldMapper(EnumFieldMapper.class);
        }

        return Optional.ofNullable(fieldMapper);
    }

    protected Optional<PropertyValueExtractor> resolvePropertyValueExtractor(MetaPropertyPath propertyPath) {
        PropertyValueExtractor valueExtractor = null;
        if (propertyPath.getRange().isDatatype()) {
            Datatype<?> datatype = propertyPath.getRange().asDatatype();
            if (datatype instanceof FileRefDatatype) {
                valueExtractor = propertyValueExtractorProvider.getPropertyValueExtractor(FilePropertyValueExtractor.class);
            } else {
                valueExtractor = propertyValueExtractorProvider.getPropertyValueExtractor(SimplePropertyValueExtractor.class);
            }
        } else if (propertyPath.getRange().isClass()) {
            valueExtractor = propertyValueExtractorProvider.getPropertyValueExtractor(ReferencePropertyValueExtractor.class);
        } else if (propertyPath.getRange().isEnum()) {
            valueExtractor = propertyValueExtractorProvider.getPropertyValueExtractor(EnumPropertyValueExtractor.class);
        }
        return Optional.ofNullable(valueExtractor);
    }


}
