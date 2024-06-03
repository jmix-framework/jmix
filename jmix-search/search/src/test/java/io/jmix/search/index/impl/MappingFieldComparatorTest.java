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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MappingFieldComparatorTest {

    @Test
    void isLeafField_null() {
        MappingFieldComparator comparator = new MappingFieldComparator();
        assertThrows(Exception.class, () -> comparator.isLeafField(null));
    }
    @Test
    void isLeafField_empty() {
        MappingFieldComparator comparator = new MappingFieldComparator();
        assertFalse(comparator.isLeafField(Collections.emptyMap()));
    }

    @Test
    void isLeafField_some_keys() {
        MappingFieldComparator comparator = new MappingFieldComparator();
        assertFalse(comparator.isLeafField(Map.of("key1", new Object(), "key2", new Object())));
    }

    @Test
    void isLeafField_some_keys_with_type_key() {
        MappingFieldComparator comparator = new MappingFieldComparator();
        assertTrue(comparator.isLeafField(Map.of("key1", new Object(), "key2", new Object(), "type", "someThing")));
    }

    @Test
    void isLeafField_type_key_only() {
        MappingFieldComparator comparator = new MappingFieldComparator();
        assertTrue(comparator.isLeafField(Map.of("type", "someThing")));
    }

    @Test
    void compareLeafFields_null_argument() {
        MappingFieldComparator comparator = new MappingFieldComparator();

        assertThrows(Exception.class, ()->comparator.compareLeafFields(null, null));
        assertThrows(Exception.class, ()->comparator.compareLeafFields(Collections.emptyMap(), null));
        assertThrows(Exception.class, ()->comparator.compareLeafFields(null, Collections.emptyMap()));
    }

    @Test
    void compareLeafFields_equal() {
        MappingFieldComparator comparator = new MappingFieldComparator();
        Map<String, Object> fieldSettings1 = Map.of("key1", "value1", "key2", "value2", "key3", "value3");
        Map<String, Object> fieldSettings2 = Map.of("key1", "value1", "key2", "value2", "key3", "value3");

        assertEquals(IndexMappingComparator.MappingComparingResult.MAPPINGS_ARE_EQUAL, comparator.compareLeafFields(fieldSettings1, fieldSettings2));
    }


    @Test
    void compareLeafFields_not_compatible() {
        MappingFieldComparator comparator = new MappingFieldComparator();
        Map<String, Object> fieldSettings1 = Map.of("key1", "value1", "key2", "value2", "key3", "value3");
        Map<String, Object> fieldSettings2 = Map.of("key1", "value1", "key2", "value2");

        assertEquals(IndexMappingComparator.MappingComparingResult.MAPPINGS_NOT_COMPATIBLE, comparator.compareLeafFields(fieldSettings1, fieldSettings2));
    }

    @Test
    void compareLeafFields_not_compatible2() {
        MappingFieldComparator comparator = new MappingFieldComparator();
        Map<String, Object> fieldSettings1 = Map.of("key1", "value1", "key2", "value2");
        Map<String, Object> fieldSettings2 = Map.of("key1", "value1", "key2", "value2", "key3", "value3");

        assertEquals(IndexMappingComparator.MappingComparingResult.MAPPINGS_NOT_COMPATIBLE, comparator.compareLeafFields(fieldSettings1, fieldSettings2));
    }

    @Test
    void compareLeafFields_not_compatible3() {
        MappingFieldComparator comparator = new MappingFieldComparator();
        Map<String, Object> fieldSettings1 = Map.of("key1", "value1", "key2", "value2", "key3", "value3");
        Map<String, Object> fieldSettings2 = Map.of("key1", "value1", "key2", "value2", "key3", "value3+");

        assertEquals(IndexMappingComparator.MappingComparingResult.MAPPINGS_NOT_COMPATIBLE, comparator.compareLeafFields(fieldSettings1, fieldSettings2));
    }

}