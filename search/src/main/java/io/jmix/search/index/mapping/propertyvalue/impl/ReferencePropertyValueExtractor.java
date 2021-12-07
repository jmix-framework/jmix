/*
 * Copyright 2021 Haulmont.
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("search_ReferencePropertyValueExtractor")
public class ReferencePropertyValueExtractor extends AbstractPropertyValueExtractor {

    protected final MetadataTools metadataTools;

    @Autowired
    public ReferencePropertyValueExtractor(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    protected boolean isSupported(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        return propertyPath.getRange().isClass();
    }

    @Override
    protected JsonNode transformSingleValue(Object value, Map<String, Object> parameters) {
        String instanceName = metadataTools.getInstanceName(value);
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put(Constants.INSTANCE_NAME_FIELD, instanceName);
        return result;
    }

    @Override
    protected JsonNode transformMultipleValues(Iterable<?> values, Map<String, Object> parameters) {
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        for (Object value : values) {
            arrayNode.add(metadataTools.getInstanceName(value));
        }
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.set(Constants.INSTANCE_NAME_FIELD, arrayNode);
        return result;
    }
}
