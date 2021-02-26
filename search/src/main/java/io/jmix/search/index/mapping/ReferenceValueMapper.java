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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;

import java.util.Map;

public class ReferenceValueMapper implements ValueMapper {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected final MetadataTools metadataTools;

    public ReferenceValueMapper(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    public JsonNode getValue(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        JsonNode result = NullNode.getInstance();
        if(propertyPath.getRange().isClass()) {
            Object refEntity = EntityValues.getValueEx(entity, propertyPath);
            if(refEntity != null) {
                String instanceName = metadataTools.getInstanceName(refEntity);
                JsonNode instanceNameNode = objectMapper.convertValue(instanceName, JsonNode.class);
                ObjectNode node = JsonNodeFactory.instance.objectNode();
                node.set("_instance_name", instanceNameNode);
                result = node;
            }
        }
        return result;
    }
}
