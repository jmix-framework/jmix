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

    public ComparingState compare(Map<String, Object> searchIndexMapping, Map<String, Object> applicationMapping) {

        if (!applicationMapping.keySet().containsAll(searchIndexMapping.keySet())) {
            return ComparingState.NOT_COMPATIBLE;
        }

        if (mappingFieldComparator.isLeafField(searchIndexMapping)) {
            if (mappingFieldComparator.isLeafField(applicationMapping)) {
                return mappingFieldComparator.compareLeafFields(searchIndexMapping, applicationMapping);
            } else {
                return ComparingState.NOT_COMPATIBLE;
            }
        }

        ComparingState result = ComparingState.EQUAL;
        for (Map.Entry<String, Object> mapEntry : searchIndexMapping.entrySet()) {
            if (!(mapEntry.getValue() instanceof Map)) {
                return ComparingState.NOT_COMPATIBLE;
            }

            ComparingState currentResult = compare((Map<String, Object>) mapEntry.getValue(), (Map<String, Object>) applicationMapping.get(mapEntry.getKey()));
            if (currentResult == ComparingState.NOT_COMPATIBLE) return ComparingState.NOT_COMPATIBLE;
            if (currentResult == ComparingState.COMPATIBLE && result != ComparingState.COMPATIBLE) {
                result = ComparingState.COMPATIBLE;
            }
        }

        if (result == ComparingState.EQUAL && applicationMapping.size() > searchIndexMapping.size()) {
            return ComparingState.COMPATIBLE;
        }

        return result;
    }

}