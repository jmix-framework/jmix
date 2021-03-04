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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.jmix.core.metamodel.model.MetaPropertyPath;

public class SimpleValueMapper extends AbstractValueMapper {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean isSupported(Object entity, MetaPropertyPath propertyPath) {
        return propertyPath.getRange().isDatatype() || propertyPath.getRange().isEnum();
    }

    @Override
    protected JsonNode processSingleValue(Object value) {
        return objectMapper.convertValue(value, JsonNode.class);
    }

    @Override
    protected JsonNode processMultipleValues(Iterable<?> values) {
        ArrayNode result = JsonNodeFactory.instance.arrayNode();
        for (Object value : values) {
            result.add(objectMapper.convertValue(value, JsonNode.class));
        }
        return result;
    }
}
