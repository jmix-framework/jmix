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

package io.jmix.search.index.mapping.strategy;

import io.jmix.core.CoreProperties;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.FileRefDatatype;
import io.jmix.core.metamodel.datatype.impl.StringDatatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.SearchApplicationProperties;
import io.jmix.search.utils.FileProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves Field and Value Mappers for Auto Mapping Strategy.
 */
@Component("search_AutoMapFieldMapperResolver")
public class AutoMapFieldMapperResolver { //todo move to automap strategy?

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected SearchApplicationProperties searchApplicationProperties;
    @Autowired
    protected FileProcessor fileProcessor;
    @Autowired
    protected Messages messages;
    @Autowired
    protected CoreProperties coreProperties;

    /**
     * Gets {@link FieldMapper} for provided {@link MetaPropertyPath}.
     *
     * @param propertyPath property
     * @return {@link FieldMapper}
     */
    public Optional<FieldMapper> getFieldMapper(MetaPropertyPath propertyPath) {
        FieldMapper fieldMapper = null;
        if (propertyPath.getRange().isDatatype()) {
            Datatype<?> datatype = propertyPath.getRange().asDatatype();
            if (datatype instanceof StringDatatype) {
                fieldMapper = new TextFieldMapper();
            } else if (datatype instanceof FileRefDatatype) {
                fieldMapper = new FileFieldMapper();
            }
        } else if (propertyPath.getRange().isClass()) {
            fieldMapper = new ReferenceFieldMapper();
        } else if (propertyPath.getRange().isEnum()) {
            fieldMapper = new EnumFieldMapper();
        }

        return Optional.ofNullable(fieldMapper);
    }

    /**
     * Checks if {@link FieldMapper} exists for provided {@link MetaPropertyPath}.
     *
     * @param propertyPath property
     * @return true if {@link FieldMapper} exists for provided {@link MetaPropertyPath}, false otherwise
     */
    public boolean hasFieldMapper(MetaPropertyPath propertyPath) {
        return getFieldMapper(propertyPath).isPresent();
    }

    /**
     * Gets {@link ValueMapper} for provided {@link MetaPropertyPath}.
     *
     * @param propertyPath property
     * @return {@link ValueMapper}
     */
    public Optional<ValueMapper> getValueMapper(MetaPropertyPath propertyPath) {
        ValueMapper valueMapper = null;
        if (propertyPath.getRange().isDatatype()) {
            Datatype<?> datatype = propertyPath.getRange().asDatatype();
            if (datatype instanceof FileRefDatatype) {
                valueMapper = new FileValueMapper(searchApplicationProperties.isAutoMapIndexFileContent(), fileProcessor);
            } else {
                valueMapper = new SimpleValueMapper();
            }
        } else if (propertyPath.getRange().isClass()) {
            valueMapper = new ReferenceValueMapper(metadataTools);
        } else if (propertyPath.getRange().isEnum()) {
            valueMapper = new EnumValueMapper(messages, coreProperties);
        }
        return Optional.ofNullable(valueMapper);
    }

    /**
     * Checks if {@link ValueMapper} exists for provided {@link MetaPropertyPath}.
     *
     * @param propertyPath property
     * @return true if {@link ValueMapper}  exists for provided {@link MetaPropertyPath}, false otherwise
     */
    public boolean hasValueMapper(MetaPropertyPath propertyPath) {
        return getValueMapper(propertyPath).isPresent();
    }
}
