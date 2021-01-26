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
import io.jmix.core.metamodel.datatype.impl.StringDatatype;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("search_AutoMapFieldMapperResolver")
public class AutoMapFieldMapperResolver { //todo move to automap strategy?

    @Autowired
    protected MetadataTools metadataTools;

    public Optional<FieldMapper> getFieldMapper(MetaPropertyPath propertyPath) {
        Optional<FieldMapper> result = Optional.empty();

        if(propertyPath.getRange().isDatatype()) {
            Datatype<?> datatype = propertyPath.getRange().asDatatype();
            if(datatype instanceof StringDatatype) {
                result = Optional.of(new TextMapper());
            } else {
                //todo
            }

        } else if(propertyPath.getRange().isClass()) {
            result = Optional.of(new ReferenceFieldMapper());
        } else if(propertyPath.getRange().isEnum()) {
            // todo
        } else {
            // todo
        }

        return result;
    }

    public boolean hasFieldMapper(MetaPropertyPath propertyPath) {
        return getFieldMapper(propertyPath).isPresent();
    }

    public Optional<ValueMapper> getValueMapper(MetaPropertyPath propertyPath) {
        return Optional.of(new SimpleValueMapper(metadataTools)); //todo check availability?
    }

    public boolean hasValueMapper(MetaPropertyPath propertyPath) {
        return getValueMapper(propertyPath).isPresent();
    }
}
