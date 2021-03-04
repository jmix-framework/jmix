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

package io.jmix.search.index.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaPropertyPath;

public class ReferenceValueMapper extends AbstractValueMapper {

    protected final MetadataTools metadataTools;

    public ReferenceValueMapper(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    protected boolean isSupported(Object entity, MetaPropertyPath propertyPath) {
        return propertyPath.getRange().isClass();
    }

    @Override
    protected JsonNode processSingleValue(Object value) {
        String instanceName = metadataTools.getInstanceName(value);
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("_instance_name", instanceName);
        return result;
    }

    @Override
    protected JsonNode processMultipleValues(Iterable<?> values) {
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        for(Object value : values) {
            arrayNode.add(metadataTools.getInstanceName(value));
        }
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.set("_instance_name", arrayNode);
        return result;
    }
}
