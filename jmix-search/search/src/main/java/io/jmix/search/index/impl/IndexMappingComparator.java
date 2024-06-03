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

import org.springframework.stereotype.Component;

import java.util.Map;

@Component("search_SearchMappingComparator")
public class IndexMappingComparator {

    private final MappingFieldComparator mappingFieldComparator;

    public IndexMappingComparator(MappingFieldComparator mappingFieldComparator) {
        this.mappingFieldComparator = mappingFieldComparator;
    }

    public MappingComparingResult compare(Map<String, Object> searchIndexMapping, Map<String, Object> applicationMapping) {

        if (!applicationMapping.keySet().containsAll(searchIndexMapping.keySet())) {
            return MappingComparingResult.MAPPINGS_NOT_COMPATIBLE;
        }

        if (mappingFieldComparator.isLeafField(searchIndexMapping)) {
            if (mappingFieldComparator.isLeafField(applicationMapping)) {
                return mappingFieldComparator.compareLeafFields(searchIndexMapping, applicationMapping);
            } else {
                return MappingComparingResult.MAPPINGS_NOT_COMPATIBLE;
            }
        }

        MappingComparingResult result = MappingComparingResult.MAPPINGS_ARE_EQUAL;
        for (Map.Entry<String, Object> mapEntry : searchIndexMapping.entrySet()) {
            if (!(mapEntry.getValue() instanceof Map)) {
                return MappingComparingResult.MAPPINGS_NOT_COMPATIBLE;
            }

            MappingComparingResult currentResult = compare((Map<String, Object>) mapEntry.getValue(), (Map<String, Object>) applicationMapping.get(mapEntry.getKey()));
            if (currentResult == MappingComparingResult.MAPPINGS_NOT_COMPATIBLE) return MappingComparingResult.MAPPINGS_NOT_COMPATIBLE;
            if (currentResult == MappingComparingResult.INDEX_MAPPING_CAN_BE_UPDATED && result != MappingComparingResult.INDEX_MAPPING_CAN_BE_UPDATED) {
                result = MappingComparingResult.INDEX_MAPPING_CAN_BE_UPDATED;
            }
        }

        if (result == MappingComparingResult.MAPPINGS_ARE_EQUAL && applicationMapping.size() > searchIndexMapping.size()) {
            return MappingComparingResult.INDEX_MAPPING_CAN_BE_UPDATED;
        }

        return result;
    }

    enum MappingComparingResult implements ConfigurationPartComparingResult{
        MAPPINGS_ARE_EQUAL,
        INDEX_MAPPING_CAN_BE_UPDATED,
        MAPPINGS_NOT_COMPATIBLE;

        @Override
        public boolean recreatingIndexIsRequired() {
            return this == MAPPINGS_NOT_COMPATIBLE;
        }

        @Override
        public boolean configurationUpdateIsRequired() {
            return this == INDEX_MAPPING_CAN_BE_UPDATED;
        }
    }
}
