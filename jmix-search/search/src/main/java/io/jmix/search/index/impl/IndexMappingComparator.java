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

import static io.jmix.search.index.impl.MappingComparingResult.*;

/**
 * Compares both the mapping from the application's index configuration
 * and the index state from the search server.
 * @param <TState> the received from the server index state type
 * @param <TJsonp> the specific for the search client Jsonp type
 */
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


    MappingComparingResult compare(Map<String, Object> appliedMapping, Map<String, Object> expectedMapping) {

        Map<String, Object> filteredSearchIndexMapping = getFilteredMapping(appliedMapping);
        if (!expectedMapping.keySet().containsAll(filteredSearchIndexMapping.keySet())) {
            return NOT_COMPATIBLE;
        }

        if (mappingFieldComparator.isLeafField(filteredSearchIndexMapping)) {
            if (mappingFieldComparator.isLeafField(expectedMapping)) {
                return mappingFieldComparator.compareLeafFields(filteredSearchIndexMapping, expectedMapping);
            } else {
                return NOT_COMPATIBLE;
            }
        }

        MappingComparingResult result = EQUAL;
        for (Map.Entry<String, Object> mapEntry : filteredSearchIndexMapping.entrySet()) {
            if (!(mapEntry.getValue() instanceof Map)) {
                return NOT_COMPATIBLE;
            }

            MappingComparingResult currentResult = compare((Map<String, Object>) mapEntry.getValue(), (Map<String, Object>) expectedMapping.get(mapEntry.getKey()));
            if (currentResult == NOT_COMPATIBLE) return NOT_COMPATIBLE;
            if (currentResult == UPDATABLE && result != UPDATABLE) {
                result = UPDATABLE;
            }
        }

        if (result == EQUAL && expectedMapping.size() > filteredSearchIndexMapping.size()) {
            return UPDATABLE;
        }

        return result;
    }

    /**
     * Removes not necessary tag from serialized mapping. This tag is added by the 'client' of the search engine.
     * @param searchIndexMapping - serialized mapping with not necessary 'type: object' elements
     * @return filtered mapping without 'type: object' elements
     */
    protected static Map<String, Object> getFilteredMapping(Map<String, Object> searchIndexMapping) {
        return searchIndexMapping
                .entrySet()
                .stream()
                .filter(e -> !(e.getKey().equals("type") && "object".equals(e.getValue())))
                //It is so because of JDK Bug https://bugs.openjdk.org/browse/JDK-8148463
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }

}
