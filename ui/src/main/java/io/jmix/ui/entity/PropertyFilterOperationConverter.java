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

package io.jmix.ui.entity;

import io.jmix.ui.component.PropertyFilter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class PropertyFilterOperationConverter implements AttributeConverter<PropertyFilter.Operation, String> {
    @Override
    public String convertToDatabaseColumn(PropertyFilter.Operation attribute) {
        return attribute != null
                ? attribute.name()
                : null;
    }

    @Override
    public PropertyFilter.Operation convertToEntityAttribute(String dbData) {
        return dbData != null
                ? PropertyFilter.Operation.valueOf(dbData)
                : null;
    }
}
