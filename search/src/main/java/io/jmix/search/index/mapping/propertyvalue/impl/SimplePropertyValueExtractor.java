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

package io.jmix.search.index.mapping.propertyvalue.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("search_SimplePropertyValueExtractor")
public class SimplePropertyValueExtractor extends AbstractPropertyValueExtractor {

    @Override
    protected boolean isSupported(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        return propertyPath.getRange().isDatatype() || propertyPath.getRange().isEnum();
    }

    @Override
    protected JsonNode transformSingleValue(Object value, Map<String, Object> parameters) {
        return objectMapper.convertValue(value, JsonNode.class);
    }

    @Override
    protected JsonNode transformMultipleValues(Iterable<?> values, Map<String, Object> parameters) {
        ArrayNode result = JsonNodeFactory.instance.arrayNode();
        for (Object value : values) {
            result.add(objectMapper.convertValue(value, JsonNode.class));
        }
        return result;
    }
}
