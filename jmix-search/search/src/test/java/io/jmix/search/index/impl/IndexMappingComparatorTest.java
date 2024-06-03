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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IndexMappingComparatorTest {

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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.EQUAL, result);
    }

    @Test
    void compare_compatible_one_level() {

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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.COMPATIBLE, result);
    }

    @Test
    void compare_not_not_compatible_one_level() {
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_empty_application_mapping() {
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


        Map<String, Object> applicationMapping = Collections.emptyMap();

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_empty_search_mapping() {
        Map<String, Object> searchIndexMapping = Collections.emptyMap();

        Map<String, Object> applicationMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "number",
                                Map.of("type", "text"),
                                "product",
                                Map.of("type", "text")
                        )
                );

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.COMPATIBLE, result);
    }

    @Test
    void compare_not_compatible_type_is_absent() {
        Map<String, Object> mappingPart = new HashMap<>();
        mappingPart.put("field1_1", Map.of("type", "text"));
        mappingPart.put("field1_2", null);
        Map<String, Object> searchIndexMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "field1",
                                Map.of("type", "text"),
                                "field2",
                                Map.of("type", "text"),
                                "referenceField1",
                                mappingPart
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_not_compatible_type_is_object() {
        Map<String, Object> mappingPart = new HashMap<>();
        mappingPart.put("field1_1", Map.of("type", "text"));
        mappingPart.put("field1_2", new Object());
        Map<String, Object> searchIndexMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "field1",
                                Map.of("type", "text"),
                                "field2",
                                Map.of("type", "text"),
                                "referenceField1",
                                mappingPart
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_not_compatible_something_strange_instead_of_type() {
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
                                        Map.of("notType", "text")
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_not_compatible_something_strange_instead_of_type_2() {
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
                                        new Object()
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_different_types() {
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
                                        Map.of("type", "number")
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_not_string_value_in_type() {
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
                                        Map.of("type", new Object())
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_type_value_is_null() {
        Map<String, Object> typeMap = new HashMap<>();
        typeMap.put("type", null);
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
                                        typeMap
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_something_strange_with_type() {
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
                                        Map.of("type", "text", "byaka", "byaka-value")
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    @Test
    void compare_empty_children_properties() {
        Map<String, Object> searchIndexMapping =
                Map.of(
                        "properties",
                        Map.of(
                                "field1",
                                Map.of("type", "text"),
                                "field2",
                                Map.of("type", "text"),
                                "referenceField1",
                                Collections.emptyMap()
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.COMPATIBLE, result);
    }

    @Test
    void compare_children_properties_is_null() {
        Map<String, Map<?, ?>> propertiesMap = new HashMap<>();
        propertiesMap.put("field1", Map.of("type", "text"));
        propertiesMap.put("field2", Map.of("type", "text"));
        propertiesMap.put("referenceField1", null);
        Map<String, Object> searchIndexMapping =
                Map.of(
                        "properties",
                        propertiesMap
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

        IndexMappingComparator comparator = new IndexMappingComparator(new MappingFieldComparator());
        ComparingState result = comparator.compare(searchIndexMapping, applicationMapping);

        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }
}