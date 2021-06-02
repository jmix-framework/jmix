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

package io.jmix.search.index.mapping.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Base class for {@link PropertyValueExtractor} implementations.
 */
public abstract class AbstractPropertyValueExtractor implements PropertyValueExtractor {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JsonNode getValue(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        JsonNode result = NullNode.getInstance();
        if (isSupported(entity, propertyPath, parameters)) {
            Object value = getFlatValueOrNull(entity, propertyPath, parameters);
            if (value != null) {
                result = processValue(value, parameters);
            }
        }
        return result;
    }

    /**
     * Checks if this value mapper supports value extraction of provided property.
     *
     * @param entity       instance
     * @param propertyPath property
     * @return true if value mapper supports this property, false otherwise
     */
    protected abstract boolean isSupported(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters);

    /**
     * Transform extracted value of single-value property into result json.
     *
     * @param value value of single-value property
     * @return value as json
     */
    protected abstract JsonNode transformSingleValue(Object value, Map<String, Object> parameters);

    /**
     * Transform extracted value of multi-value property into result json.
     *
     * @param values values of multi-value property
     * @return value as json
     */
    protected abstract JsonNode transformMultipleValues(Iterable<?> values, Map<String, Object> parameters);

    protected boolean isCollection(Object value) {
        return !(value instanceof Entity) && value instanceof Iterable;
    }

    protected JsonNode processValue(Object value, Map<String, Object> parameters) {
        if (isCollection(value)) {
            JsonNode result = transformMultipleValues((Iterable<?>) value, parameters);
            return result.isEmpty() ? NullNode.getInstance() : result;
        } else {
            return transformSingleValue(value, parameters);
        }
    }

    @Nullable
    protected Object getFlatValueOrNull(Object entity, MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        String[] properties = propertyPath.getPropertyNames();

        Object currentValue = null;
        Object currentEntity = entity;
        for (String property : properties) {
            if (currentEntity == null) {
                break;
            }

            if (currentEntity instanceof Entity) {
                currentValue = EntityValues.getValue(currentEntity, property);
                if (currentValue == null) {
                    break;
                }

                if (currentValue instanceof Entity || currentValue instanceof Iterable<?>) {
                    currentEntity = currentValue;
                } else {
                    currentEntity = null;
                }
            } else if (currentEntity instanceof Iterable<?>) {
                List<Object> elementValues = new ArrayList<>();
                for (Object element : (Iterable<?>) currentEntity) {
                    Object elementValue = EntityValues.getValue(element, property);
                    if (!(elementValue instanceof Entity) && elementValue instanceof Iterable<?>) {
                        StreamSupport.stream(((Iterable<?>) elementValue).spliterator(), false).forEach(elementValues::add);
                    } else if (elementValue != null) {
                        elementValues.add(elementValue);
                    }
                }
                currentValue = elementValues;
                currentEntity = elementValues;
            }
        }

        return currentValue;
    }
}
