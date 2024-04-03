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
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.dynattr.model.CategoryAttributeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class DynAttrPropertyValueExtractor extends AbstractPropertyValueExtractor {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private Metadata metadata;
    @Autowired
    private FetchPlans fetchPlans;

    @Override
    protected boolean isSupported(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        return propertyPath.getFirstPropertyName().startsWith("+");
    }

    @Override
    protected JsonNode transformSingleValue(Object value, Map<String, Object> parameters) {
        return objectMapper.convertValue(value, JsonNode.class);
    }
    @Override
    protected JsonNode transformMultipleValues(Iterable<?> values, Map<String, Object> parameters) {
        return null;
    }

    @Override
    protected boolean isCollection(Object value) {
        return super.isCollection(value);
    }

    @Override
    protected JsonNode processValue(Object value, Map<String, Object> parameters) {
        return super.processValue(value, parameters);
    }

    @Override
    protected Object getFlatValueOrNull(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        FetchPlan fetchPlan = fetchPlans.builder(CategoryAttributeValue.class)
                .add("categoryAttribute", FetchPlan.BASE)
                .add("entity", FetchPlan.BASE)
                .add("intValue")
                .add("doubleValue")
                .add("stringValue")
                .add("decimalValue")
                .add("booleanValue")
                .add("dateValue")
                .add("dateWithoutTimeValue")
                .build();

        CategoryAttributeValue one = dataManager.load(CategoryAttributeValue.class)
                .query("select e from dynat_CategoryAttributeValue e where e.categoryAttribute.name = :code and e.categoryAttribute.categoryEntityType = :entityType and e.entity.entityId = :entityId")
                .parameter("code", propertyPath.getFirstPropertyName().substring(1))
                .parameter("entityId", EntityValues.getId(entity))
                .parameter("entityType", metadata.getClass(entity).getName())
                .fetchPlan(fetchPlan)
                .one();
        if(StringUtils.hasText(one.getStringValue())) {
            return one.getStringValue();
        } else if(one.getIntValue() != null) {
            return one.getIntValue();
        } else if(one.getDoubleValue() != null) {
            return one.getDoubleValue();
        } else if(one.getDecimalValue() != null) {
            return one.getDecimalValue();
        } else if(one.getDateValue() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(one.getDateValue());
        } else if(one.getBooleanValue() != null) {
            return one.getBooleanValue();
        } else if(one.getDateWithoutTimeValue() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return formatter.format(one.getDateWithoutTimeValue());
        } else {
            throw new IllegalStateException("Unknown type");
        }


    }
}
