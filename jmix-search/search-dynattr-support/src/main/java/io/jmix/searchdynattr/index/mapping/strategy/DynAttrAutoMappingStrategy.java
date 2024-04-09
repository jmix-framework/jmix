/*
 * Copyright 2024 Haulmont.
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

package io.jmix.searchdynattr.index.mapping.strategy;

import io.jmix.core.DataManager;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.FileRefDatatype;
import io.jmix.core.metamodel.datatype.impl.StringDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.model.CategoryAttribute;
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
import io.jmix.search.index.mapping.strategy.impl.AutoMappingStrategy;
import io.jmix.searchdynattr.index.mapping.propertyvalue.BaseDynAttrPropertyValueExtractor;
import io.jmix.searchdynattr.index.mapping.propertyvalue.EntityDynAttrValueExtractor;
import io.jmix.searchdynattr.index.mapping.propertyvalue.EnumDynAttrValueExtractor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Primary
@Component("search_dynattr_support_DynAttrAutoMappingStrategy")
public class DynAttrAutoMappingStrategy extends AutoMappingStrategy {
    protected final DataManager dataManager;

    public DynAttrAutoMappingStrategy(PropertyValueExtractorProvider propertyValueExtractorProvider,
                                      FieldMapperProvider fieldMapperProvider,
                                      DataManager dataManager) {
        super(propertyValueExtractorProvider, fieldMapperProvider);
        this.dataManager = dataManager;
    }

    @SuppressWarnings("DuplicatedCode")
    protected Optional<FieldMapper> resolveFieldMapper(MetaPropertyPath propertyPath) {
        FieldMapper fieldMapper = null;
        if (propertyPath.getFirstPropertyName().startsWith("+")) {
            MetaClass domain = propertyPath.getMetaClass();
            Optional<CategoryAttribute> categoryAttribute = dataManager.load(CategoryAttribute.class)
                    .query("select e from dynat_CategoryAttribute e where e.category.entityType = :entityType and e.name = :name")
                    .parameter("entityType", domain.getName())
                    .parameter("name", propertyPath.getFirstPropertyName().substring(1))
                    .optional();
            return categoryAttribute.map(e -> resolveDynAttrFieldMapper(e.getDataType()));
        } else if (propertyPath.getRange().isDatatype()) {
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

    private FieldMapper resolveDynAttrFieldMapper(AttributeType dataType) {
        switch (dataType) {
            // Note: do not replace this switch with if statement to prevent undefined
            // behaviour in further dynattr types expansion
            case STRING, BOOLEAN, DATE_WITHOUT_TIME, DATE, DECIMAL, DOUBLE, INTEGER -> {
                return fieldMapperProvider.getFieldMapper(TextFieldMapper.class);
            }
            case ENTITY -> {
                return fieldMapperProvider.getFieldMapper(ReferenceFieldMapper.class);
            }
            case ENUMERATION -> {
                return fieldMapperProvider.getFieldMapper(EnumFieldMapper.class);
            }
            default -> {
                throw new IllegalArgumentException("Unknown field");
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    protected Optional<PropertyValueExtractor> resolvePropertyValueExtractor(MetaPropertyPath propertyPath) {
        PropertyValueExtractor valueExtractor = null;
        if (propertyPath.getFirstPropertyName().startsWith("+")) {
            MetaClass domain = propertyPath.getMetaClass();
            Optional<CategoryAttribute> categoryAttribute = dataManager.load(CategoryAttribute.class)
                    .query("select e from dynat_CategoryAttribute e where e.category.entityType = :entityType and e.name = :name")
                    .parameter("entityType", domain.getName())
                    .parameter("name", propertyPath.getFirstPropertyName().substring(1))
                    .optional();
            return categoryAttribute.map(this::resolveDynAttrPropertyValueExtractor);
        } else if (propertyPath.getRange().isDatatype()) {
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

    private PropertyValueExtractor resolveDynAttrPropertyValueExtractor(CategoryAttribute categoryAttribute) {
        return switch (categoryAttribute.getDataType()) {
            case STRING, INTEGER, DOUBLE, DECIMAL, BOOLEAN, DATE, DATE_WITHOUT_TIME ->
                    propertyValueExtractorProvider.getPropertyValueExtractor(BaseDynAttrPropertyValueExtractor.class);
            case ENTITY -> propertyValueExtractorProvider.getPropertyValueExtractor(EntityDynAttrValueExtractor.class);
            case ENUMERATION -> propertyValueExtractorProvider.getPropertyValueExtractor(EnumDynAttrValueExtractor.class);
        };
    }

}
