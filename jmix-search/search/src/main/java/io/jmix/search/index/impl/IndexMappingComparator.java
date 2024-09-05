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

package io.jmix.search.index.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.IndexConfiguration;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class IndexMappingComparator<TState, TJsonp> {
    private static final Logger log = LoggerFactory.getLogger(IndexMappingComparator.class);
    protected static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {
    };

    protected final MappingFieldComparator mappingFieldComparator;
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final JsonpSerializer<TJsonp> jsonpSerializer;


    public IndexMappingComparator(MappingFieldComparator mappingFieldComparator, JsonpSerializer<TJsonp> jsonpSerializer) {
        this.mappingFieldComparator = mappingFieldComparator;
        this.jsonpSerializer = jsonpSerializer;
    }

    public MappingComparingResult compare(IndexConfiguration indexConfiguration, TState currentIndexState) {

        Map<String, Object> appliedMapping = getAppliedMapping(currentIndexState);
        Map<String, Object> expectedMapping = getExpectedMapping(indexConfiguration);
        log.debug("Mappings of index '{}':\nCurrent: {}\nActual: {}",
                indexConfiguration.getIndexName(), appliedMapping, expectedMapping);
        return compare(appliedMapping, expectedMapping);
    }

    protected Map<String, Object> getExpectedMapping(IndexConfiguration indexConfiguration) {
        return objectMapper.convertValue(indexConfiguration.getMapping(), MAP_TYPE_REF);
    }

    protected Map<String, Object> getAppliedMapping(TState currentIndexState) {
        TJsonp typeMapping = extractTypeMapping(currentIndexState);
        if (typeMapping == null) {
            return Collections.emptyMap();
        } else {
            ObjectNode currentMappingNode = jsonpSerializer.toObjectNode(typeMapping);
            return objectMapper.convertValue(currentMappingNode, MAP_TYPE_REF);
        }
    }

    @Nullable
    protected abstract TJsonp extractTypeMapping(TState currentIndexState);


    MappingComparingResult compare(Map<String, Object> searchIndexMapping, Map<String, Object> applicationMapping) {

        Map<String, Object> filteredSearchIndexMapping = getFilteredMapping(searchIndexMapping);
        if (!applicationMapping.keySet().containsAll(filteredSearchIndexMapping.keySet())) {
            return MappingComparingResult.NOT_COMPATIBLE;
        }

        if (mappingFieldComparator.isLeafField(filteredSearchIndexMapping)) {
            if (mappingFieldComparator.isLeafField(applicationMapping)) {
                return mappingFieldComparator.compareLeafFields(filteredSearchIndexMapping, applicationMapping);
            } else {
                return MappingComparingResult.NOT_COMPATIBLE;
            }
        }

        MappingComparingResult result = MappingComparingResult.EQUAL;
        for (Map.Entry<String, Object> mapEntry : filteredSearchIndexMapping.entrySet()) {
            if (!(mapEntry.getValue() instanceof Map)) {
                return MappingComparingResult.NOT_COMPATIBLE;
            }

            MappingComparingResult currentResult = compare((Map<String, Object>) mapEntry.getValue(), (Map<String, Object>) applicationMapping.get(mapEntry.getKey()));
            if (currentResult == MappingComparingResult.NOT_COMPATIBLE) return MappingComparingResult.NOT_COMPATIBLE;
            if (currentResult == MappingComparingResult.UPDATABLE && result != MappingComparingResult.UPDATABLE) {
                result = MappingComparingResult.UPDATABLE;
            }
        }

        if (result == MappingComparingResult.EQUAL && applicationMapping.size() > filteredSearchIndexMapping.size()) {
            return MappingComparingResult.UPDATABLE;
        }

        return result;
    }

    protected static Map<String, Object> getFilteredMapping(Map<String, Object> searchIndexMapping) {
        return searchIndexMapping
                .entrySet()
                .stream()
                .filter(e -> !(e.getKey().equals("type") && "object".equals(e.getValue())))
                //It is so because of JDK Bug https://bugs.openjdk.org/browse/JDK-8148463
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }

    public enum MappingComparingResult implements ConfigurationPartComparingResult {
        EQUAL,
        UPDATABLE,
        NOT_COMPATIBLE;

        @Override
        public boolean indexRecreatingIsRequired() {
            return this == NOT_COMPATIBLE;
        }

        @Override
        public boolean configurationUpdateIsRequired() {
            return this == UPDATABLE;
        }
    }
}
