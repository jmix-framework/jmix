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

package io.jmix.searchdynattr.index.mapping.propertyvalue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.jmix.core.CoreProperties;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.search.index.mapping.propertyvalue.impl.AbstractPropertyValueExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("search_dynattr_support_EnumDynAttrValueExtractor")
public class EnumDynAttrValueExtractor extends AbstractPropertyValueExtractor {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private Metadata metadata;
    @Autowired
    private CoreProperties coreProperties;
    @Autowired
    private Messages messages;
    @Autowired
    private MsgBundleTools msgBundleTools;


    @Override
    protected boolean isSupported(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        if (propertyPath.getFirstPropertyName().startsWith("+")) {
            Optional<CategoryAttribute> categoryAttributeOptional = dataManager.load(CategoryAttribute.class)
                    .query("select e from dynat_CategoryAttribute e where e.category.entityType = :entityType and e.name = :name")
                    .parameter("entityType", metadata.getClass(entity).getName())
                    .parameter("name", propertyPath.getFirstPropertyName().substring(1))
                    .optional();
            return categoryAttributeOptional.map(e -> e.getDataType().equals(AttributeType.ENUMERATION)).orElse(false);
        }
        return false;
    }

    @Override
    protected JsonNode transformSingleValue(Object value, Map<String, Object> parameters) {
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
        if (value instanceof List<?> list) {
            list.forEach(e -> arrayNode.add(e.toString()));
        }
        return arrayNode;
    }

    @Override
    protected JsonNode transformMultipleValues(Iterable<?> values, Map<String, Object> parameters) {
        ArrayNode result = JsonNodeFactory.instance.arrayNode();
        values.forEach(e -> {
            if(e instanceof Iterable<?> list) {
                ArrayNode nodeArray = JsonNodeFactory.instance.arrayNode();
                list.forEach(item -> nodeArray.add(item.toString()));
                result.add(nodeArray);
            }
        });
        return result;
    }

    @Override
    protected Object getFlatValueOrNull(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        Optional<CategoryAttribute> categoryAttributeOptional = dataManager.load(CategoryAttribute.class)
                .query("select e from dynat_CategoryAttribute e where e.category.entityType = :entityType and e.name = :name")
                .parameter("entityType", metadata.getClass(entity).getName())
                .parameter("name", propertyPath.getFirstPropertyName().substring(1))
                .optional();
        Object enumValue = categoryAttributeOptional.map(e -> EntityValues.getValue(entity, "+" + e.getCode()))
                .orElse(null);
        if (enumValue == null) {
            return null;
        }
        return getEnumValueInAllLocations(categoryAttributeOptional.get(), enumValue);
    }

    private Object getEnumValueInAllLocations(CategoryAttribute attribute, Object enumValue) {
        if (!attribute.getIsCollection()) {
            if (attribute.getEnumerationMsgBundle() == null) {
                return enumValue.toString();
            } else {
                List<String> result = new ArrayList<>();
                Map<String, String> msgBundleValues = msgBundleTools.getMsgBundleValues(attribute.getEnumerationMsgBundle());
                msgBundleValues.keySet()
                        .stream()
                        .filter(key -> extractKeyFromMessageBundle(key).equals(enumValue))
                        .forEach(key -> result.add(msgBundleValues.get(key)));
                return result;
            }

        } else if (enumValue instanceof Iterable<?> enumValues) {
            if (attribute.getEnumerationMsgBundle() != null) {
                Map<String, String> msgBundleValues = msgBundleTools.getMsgBundleValues(attribute.getEnumerationMsgBundle());
                List<List<String>> result = new ArrayList<>();
                enumValues.forEach(enumValueItem -> {
                    result.add(msgBundleValues.keySet()
                            .stream()
                            .filter(key -> extractKeyFromMessageBundle(key).equals(enumValueItem))
                            .map(msgBundleValues::get)
                            .toList());
                });
                return result;
            } else {
                return enumValues;
            }
        }
        return enumValue;
    }

    private String extractKeyFromMessageBundle(String key) {
        if(key.contains("/") && key.split("/").length > 1) {
            return key.split("/")[1];
        }
        return key;
    }
}
