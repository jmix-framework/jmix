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

package io.jmix.search.index.mapping;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.FileRefDatatype;
import io.jmix.core.metamodel.datatype.impl.StringDatatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.SearchProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("search_AutoMapFieldMapperResolver")
public class AutoMapFieldMapperResolver { //todo move to automap strategy?

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected FileProcessor fileProcessor;

    public Optional<FieldMapper> getFieldMapper(MetaPropertyPath propertyPath) {
        FieldMapper fieldMapper = null;
        if(propertyPath.getRange().isDatatype()) {
            Datatype<?> datatype = propertyPath.getRange().asDatatype();
            if(datatype instanceof StringDatatype) {
                fieldMapper = new TextFieldMapper();
            } else if (datatype instanceof FileRefDatatype) {
                fieldMapper = new FileFieldMapper();
            } else {
                //todo
            }
        } else if(propertyPath.getRange().isClass()) {
            fieldMapper = new ReferenceFieldMapper();
        } else if(propertyPath.getRange().isEnum()) {
            // todo
        } else {
            // todo
        }

        return Optional.ofNullable(fieldMapper);
    }

    public boolean hasFieldMapper(MetaPropertyPath propertyPath) {
        return getFieldMapper(propertyPath).isPresent();
    }

    public Optional<ValueMapper> getValueMapper(MetaPropertyPath propertyPath) {
        ValueMapper valueMapper = null;
        if(propertyPath.getRange().isDatatype()) {
            Datatype<?> datatype = propertyPath.getRange().asDatatype();
            if(datatype instanceof FileRefDatatype) {
                valueMapper = new FileValueMapper(searchProperties.isAutoMapIndexFileContent(), fileProcessor);
            } else {
                valueMapper = new SimpleValueMapper();
            }
        } else if(propertyPath.getRange().isClass()) {
            valueMapper = new ReferenceValueMapper(metadataTools);
        }
        return Optional.ofNullable(valueMapper);
    }

    public boolean hasValueMapper(MetaPropertyPath propertyPath) {
        return getValueMapper(propertyPath).isPresent();
    }
}
