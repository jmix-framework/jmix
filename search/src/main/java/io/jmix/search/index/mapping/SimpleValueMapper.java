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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SimpleValueMapper implements ValueMapper {

    private static final Logger log = LoggerFactory.getLogger(SimpleValueMapper.class);

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected final MetadataTools metadataTools;

    public SimpleValueMapper(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    public JsonNode getValue(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameter) {
        log.info("[IVGA] Get value of property '{}' from entity '{}'", propertyPath, entity);
        JsonNode result;

        Object value;
        if(propertyPath.getRange().isClass()) {
            Object refEntity = EntityValues.getValueEx(entity, propertyPath);
            if(refEntity != null) {
                String instanceName = metadataTools.getInstanceName(refEntity);
                log.info("[IVGA] Instance Name for entity {} = {}", entity, instanceName);
                JsonNode instanceNameNode = objectMapper.convertValue(instanceName, JsonNode.class);
                ObjectNode node = JsonNodeFactory.instance.objectNode();
                node.set("_instance_name", instanceNameNode);
                result = node;
            } else {
                result = NullNode.getInstance();
            }
        } else {
            value = EntityValues.getValueEx(entity, propertyPath);
            result = objectMapper.convertValue(value, JsonNode.class);
        }

        log.info("[IVGA] Result json: {}", result);
        return result;
    }
}
