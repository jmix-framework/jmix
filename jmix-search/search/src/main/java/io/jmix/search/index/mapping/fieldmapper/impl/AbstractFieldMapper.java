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

package io.jmix.search.index.mapping.fieldmapper.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.search.index.mapping.fieldmapper.FieldMapper;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base class for {@link FieldMapper} implementations.
 */
public abstract class AbstractFieldMapper implements FieldMapper {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Defines Elasticsearch-native parameters supported by this field mapper.
     * They will be used to build field configuration.
     *
     * @return names of supported parameters
     */
    abstract Set<String> getSupportedMappingParameters();

    /**
     * Creates map of input parameters supported by this mapper.
     * @param rawParameters input parameters
     * @return supported parameters only
     */
    protected Map<String, Object> createEffectiveParameters(Map<String, Object> rawParameters) {
        return rawParameters.entrySet().stream()
                .filter((entry) -> getSupportedMappingParameters().contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
