/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowuidata.serialization.io.jmix.uidata.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.jmix.core.EntityAttributeSerializationExtension;
import io.jmix.core.JmixOrder;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import io.jmix.flowuidata.entity.FilterConditionConverter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import jakarta.persistence.Convert;

@Component("flowui_FilterConditionAttributeSerializationExtension")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class FilterConditionAttributeSerializationExtension implements EntityAttributeSerializationExtension {

    @Override
    public boolean supports(MetaProperty property) {
        return property.getRange().isClass()
                && LogicalFilterCondition.class.isAssignableFrom(property.getRange().asClass().getJavaClass())
                && property.getAnnotatedElement().isAnnotationPresent(Convert.class)
                && property.getAnnotatedElement().getAnnotation(Convert.class).converter() == FilterConditionConverter.class;
    }

    @Nullable
    @Override
    public JsonElement toJson(MetaProperty property, @Nullable Object propertyValue) {
        if (propertyValue instanceof LogicalFilterCondition) {
            FilterConditionConverter converter = new FilterConditionConverter();
            return new JsonPrimitive(converter.convertToDatabaseColumn((LogicalFilterCondition) propertyValue));
        }

        return null;
    }

    @Nullable
    @Override
    public Object fromJson(MetaProperty property, JsonElement element) {
        FilterConditionConverter converter = new FilterConditionConverter();
        return converter.convertToEntityAttribute(element.getAsString());
    }
}
