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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public abstract class IndexMappingComparator<IndexStateType, JsonpSerializableType, ClientType> {
    protected final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {
    };
    private static final Logger log = LoggerFactory.getLogger(IndexMappingComparator.class);

    private final MappingFieldComparator mappingFieldComparator;
    protected final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonpSerializer<JsonpSerializableType, ClientType> jsonpSerializer;


    public IndexMappingComparator(MappingFieldComparator mappingFieldComparator, JsonpSerializer<JsonpSerializableType, ClientType> jsonpSerializer) {
        this.mappingFieldComparator = mappingFieldComparator;
        this.jsonpSerializer = jsonpSerializer;
    }

    public MappingComparingResult compare(IndexConfiguration indexConfiguration, IndexStateType currentIndexState, ClientType client) {

        Map<String, Object> appliedMapping = getAppliedMapping(currentIndexState, client);
        Map<String, Object> expectedMapping = getExpectedMapping(indexConfiguration);
        log.debug("Mappings of index '{}':\nCurrent: {}\nActual: {}",
                indexConfiguration.getIndexName(), appliedMapping, expectedMapping);
        return compare(appliedMapping, expectedMapping);
    }

    private Map<String, Object> getExpectedMapping(IndexConfiguration indexConfiguration) {
        return objectMapper.convertValue(indexConfiguration.getMapping(), MAP_TYPE_REF);
    }

    private Map<String, Object> getAppliedMapping(IndexStateType currentIndexState, ClientType client) {
        JsonpSerializableType typeMapping = extractTypeMapping(currentIndexState);
        if (typeMapping == null) {
            return Collections.emptyMap();
        } else {
            ObjectNode currentMappingNode = jsonpSerializer.toObjectNode(typeMapping, client);
            return objectMapper.convertValue(currentMappingNode, MAP_TYPE_REF);
        }
    }

    //TODO сделать строгую типизацию через TypeMapping
    protected abstract JsonpSerializableType extractTypeMapping(IndexStateType currentIndexState);


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
            if (currentResult == MappingComparingResult.CAN_BE_UPDATED && result != MappingComparingResult.CAN_BE_UPDATED) {
                result = MappingComparingResult.CAN_BE_UPDATED;
            }
        }

        if (result == MappingComparingResult.EQUAL && applicationMapping.size() > filteredSearchIndexMapping.size()) {
            return MappingComparingResult.CAN_BE_UPDATED;
        }

        return result;
    }

    private static Map<String, Object> getFilteredMapping(Map<String, Object> searchIndexMapping) {
        //This code contains some strange lines that is for collecting values.
        //It is so because of JDK Bug https://bugs.openjdk.org/browse/JDK-8148463
        return searchIndexMapping
                .entrySet()
                .stream()
                .filter(e -> !(e.getKey().equals("type") && "object".equals(e.getValue())))
                .collect(HashMap::new, (m, e)->m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }

    public enum MappingComparingResult implements ConfigurationPartComparingResult {
        EQUAL,
        CAN_BE_UPDATED,
        NOT_COMPATIBLE;

        @Override
        public boolean indexRecreatingIsRequired() {
            return this == NOT_COMPATIBLE;
        }

        @Override
        public boolean configurationUpdateIsRequired() {
            return this == CAN_BE_UPDATED;
        }
    }
}
