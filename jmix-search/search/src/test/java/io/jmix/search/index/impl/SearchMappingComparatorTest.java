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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SearchMappingComparatorTest {

    @Test
    void compare_equal() {
        Map<String, Object> searchIndexMapping = Map.of(
                "number",
                Map.of("type", "text"),
                "product",
                Map.of("type", "text")
        );


        Map<String, Object> applicationMapping = Map.of(
                "number",
                Map.of("type", "text"),
                "product",
                Map.of("type", "text")
        );

        SearchMappingComparator comparator = new SearchMappingComparator();
        ComparingState result = comparator.innerCompare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.EQUAL, result);
    }

    @Test
    void compare_not_additive_firstLevel() {
        Map<String, Object> searchIndexMapping = Map.of(
                "number",
                Map.of("type", "text"),
                "product",
                Map.of("type", "text")
        );


        Map<String, Object> applicationMapping = Map.of(
                "number",
                Map.of("type", "text")
        );

        SearchMappingComparator comparator = new SearchMappingComparator();
        ComparingState result = comparator.innerCompare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }
}