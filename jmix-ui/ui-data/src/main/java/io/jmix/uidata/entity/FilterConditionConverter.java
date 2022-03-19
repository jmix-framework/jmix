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

package io.jmix.uidata.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.jmix.core.DevelopmentException;
import io.jmix.ui.entity.LogicalFilterCondition;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class FilterConditionConverter implements AttributeConverter<LogicalFilterCondition, String> {

    protected ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build()
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    @Override
    public String convertToDatabaseColumn(LogicalFilterCondition filterCondition) {
        try {
            return objectMapper.writeValueAsString(filterCondition);
        } catch (JsonProcessingException e) {
            throw new DevelopmentException(e.getMessage(), e);
        }
    }

    @Override
    public LogicalFilterCondition convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, LogicalFilterCondition.class);
        } catch (JsonProcessingException e) {
            throw new DevelopmentException(e.getMessage(), e);
        }
    }
}
