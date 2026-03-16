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

package io.jmix.flowuirestds.genericfilter;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.jmix.core.EntityAttributeSerializationExtension;
import io.jmix.core.JmixOrder;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Component("flowui_RestDsFilterConditionAttributeSerializationExtension")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class FilterConditionAttributeSerializationExtension implements EntityAttributeSerializationExtension {

    protected ObjectMapper objectMapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    @Override
    public boolean supports(MetaProperty property) {
        return property.getDomain().getJavaClass().equals(FilterConfiguration.class) &&
                property.getRange().isClass() &&
                property.getRange().asClass().getJavaClass().equals(LogicalFilterCondition.class);
    }

    @Nullable
    @Override
    public JsonElement toJson(MetaProperty property, @Nullable Object propertyValue) {
        if (propertyValue instanceof LogicalFilterCondition) {
            String result;
            try {
                result = objectMapper.writeValueAsString(propertyValue);
            } catch (JacksonException e) {
                throw new RuntimeException("Error serializing LogicalFilterCondition to JSON", e);
            }
            return new JsonPrimitive(result);
        }
        return null;
    }

    @Nullable
    @Override
    public Object fromJson(MetaProperty property, JsonElement element) {
        String json = element.getAsString();
        try {
            return objectMapper.readValue(json, LogicalFilterCondition.class);
        } catch (JacksonException e) {
            throw new RuntimeException("Error deserializing LogicalFilterCondition from JSON", e);
        }
    }
}
