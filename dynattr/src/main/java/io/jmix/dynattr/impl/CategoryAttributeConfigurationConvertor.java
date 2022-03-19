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

package io.jmix.dynattr.impl;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jmix.dynattr.ConfigurationExclusionStrategy;
import io.jmix.dynattr.model.CategoryAttributeConfiguration;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class CategoryAttributeConfigurationConvertor implements AttributeConverter<CategoryAttributeConfiguration, String> {
    @Override
    public String convertToDatabaseColumn(CategoryAttributeConfiguration attribute) {
        Gson gson = new GsonBuilder().setExclusionStrategies(new ConfigurationExclusionStrategy()).create();
        return gson.toJson(attribute);
    }

    @Override
    public CategoryAttributeConfiguration convertToEntityAttribute(String dbData) {
        CategoryAttributeConfiguration configuration;
        if (!Strings.isNullOrEmpty(dbData)) {
            Gson gson = new GsonBuilder().setExclusionStrategies(new ConfigurationExclusionStrategy()).create();
            configuration = gson.fromJson(dbData, CategoryAttributeConfiguration.class);
        } else {
            configuration = new CategoryAttributeConfiguration();
        }
        return configuration;
    }
}
