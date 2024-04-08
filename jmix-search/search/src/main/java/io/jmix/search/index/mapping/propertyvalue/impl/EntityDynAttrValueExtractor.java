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

package io.jmix.search.index.mapping.propertyvalue.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.search.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class EntityDynAttrValueExtractor extends AbstractPropertyValueExtractor {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private Metadata metadata;
    @Autowired
    private MetadataTools metadataTools;

    @Override
    protected boolean isSupported(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        if (propertyPath.getFirstPropertyName().startsWith("+")) {
            Optional<CategoryAttribute> categoryAttributeOptional = dataManager.load(CategoryAttribute.class)
                    .query("select e from dynat_CategoryAttribute e where e.category.entityType = :entityType and e.name = :name")
                    .parameter("entityType", metadata.getClass(entity).getName())
                    .parameter("name", propertyPath.getFirstPropertyName().substring(1))
                    .optional();
            return categoryAttributeOptional.map(e -> e.getDataType().equals(AttributeType.ENTITY)).orElse(false);
        }
        return false;
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

    @Override
    protected Object getFlatValueOrNull(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        Optional<CategoryAttribute> categoryAttributeOptional = dataManager.load(CategoryAttribute.class)
                .query("select e from dynat_CategoryAttribute e where e.category.entityType = :entityType and e.name = :name")
                .parameter("entityType", metadata.getClass(entity).getName())
                .parameter("name", propertyPath.getFirstPropertyName().substring(1))
                .optional();
        return categoryAttributeOptional.map(e -> EntityValues.getValue(entity, "+" + e.getCode())).orElse(null);
    }
}
