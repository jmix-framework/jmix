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
    void compare_equal_one_level() {
        Map<String, Object> searchIndexMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "number",
                                Map.of("type", "text"),
                                "product",
                                Map.of("type", "text")
                        )
                );


        Map<String, Object> applicationMapping = Map.of(
                "properties",
                Map.of(
                        "number",
                        Map.of("type", "text"),
                        "product",
                        Map.of("type", "text")
                ));

        SearchMappingComparator comparator = new SearchMappingComparator();
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.EQUAL, result);
    }

    @Test
    void compare_equal_different_key_order() {
        Map<String, Object> searchIndexMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "number",
                                Map.of("type", "text"),
                                "product",
                                Map.of("type", "text")
                        )
                );


        Map<String, Object> applicationMapping = Map.of(
                "properties",
                Map.of(
                        "product",
                        Map.of("type", "text"),
                        "number",
                        Map.of("type", "text")
                ));

        SearchMappingComparator comparator = new SearchMappingComparator();
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.EQUAL, result);
    }

    @Test
    void compare_equal_two_levels() {
        Map<String, Object> searchIndexMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "field1",
                                Map.of("type", "text"),
                                "field2",
                                Map.of("type", "text"),
                                "referenceField1",
                                Map.of(
                                        "field1_1",
                                        Map.of("type", "text"),
                                        "field1_2",
                                        Map.of("type", "text")
                                )
                        )
                );


        Map<String, Object> applicationMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "referenceField1",
                                Map.of(
                                        "field1_2",
                                        Map.of("type", "text"),
                                        "field1_1",
                                        Map.of("type", "text")
                                ),
                                "field1",
                                Map.of("type", "text"),
                                "field2",
                                Map.of("type", "text")
                        )
                );

        SearchMappingComparator comparator = new SearchMappingComparator();
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.EQUAL, result);
    }

    @Test
    void compare_equal_two_levels_not_compatible() {
        Map<String, Object> searchIndexMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "field1",
                                Map.of("type", "text"),
                                "field2",
                                Map.of("type", "text"),
                                "referenceField1",
                                Map.of(
                                        "field1_1",
                                        Map.of("type", "text"),
                                        "field1_2",
                                        Map.of("type", "text")
                                )
                        )
                );


        Map<String, Object> applicationMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "referenceField1",
                                Map.of(
                                        "field1_2",
                                        Map.of("type", "text"),
                                        "field1_1",
                                        Map.of("type", "text")
                                ),
                                "field1",
                                Map.of("type", "text")
                        )
                );

        SearchMappingComparator comparator = new SearchMappingComparator();
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_not_not_compatible() {
        Map<String, Object> searchIndexMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "number",
                                Map.of("type", "text"),
                                "product",
                                Map.of("type", "text")
                        )
                );


        Map<String, Object> applicationMapping = Map.of(
                "properties",
                Map.of(
                        "number",
                        Map.of("type", "text")
                ));

        SearchMappingComparator comparator = new SearchMappingComparator();
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_compatible() {

        Map<String, Object> searchIndexMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "number",
                                Map.of("type", "text"),
                                "product",
                                Map.of("type", "text")
                        )
                );


        Map<String, Object> applicationMapping = Map.of(
                "properties",
                Map.of(
                        "number",
                        Map.of("type", "text"),
                        "product",
                        Map.of("type", "text"),
                        "field3",
                        Map.of("type", "text")
                ));

        SearchMappingComparator comparator = new SearchMappingComparator();
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.COMPATIBLE, result);
    }
}