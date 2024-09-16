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

package io.jmix.search.index.impl

import spock.lang.Specification

import static io.jmix.search.index.impl.MappingComparingResult.*
import static java.util.Collections.emptyMap


class MappingFieldComparatorTest extends Specification {

    def "isLeafField. The argument is null"() {
        given:
        MappingFieldComparator comparator = new MappingFieldComparator()

        when:
        comparator.isLeafField(null)

        then:
        thrown(Exception.class);
    }

    def "isLeafField. The argument is empty Map"() {
        given:
        MappingFieldComparator comparator = new MappingFieldComparator()

        expect:
        !comparator.isLeafField(emptyMap())
    }

    def "isLeafField. Some keys"() {
        given:
        MappingFieldComparator comparator = new MappingFieldComparator()

        expect:
        !comparator.isLeafField(Map.of("key1", new Object(), "key2", new Object()))
    }

    def "isLeafField. Some keys with the 'type' key"() {
        given:
        MappingFieldComparator comparator = new MappingFieldComparator();

        expect:
        comparator.isLeafField(Map.of("key1", new Object(), "key2", new Object(), "type", "someThing"))
    }

    def "isLeafField. The 'type' key only"() {
        given:
        MappingFieldComparator comparator = new MappingFieldComparator();

        expect:
        comparator.isLeafField(Map.of("type", "someThing"))
    }

    def "compareLeafFields. Null arguments are not supported."() {
        given:
        MappingFieldComparator comparator = new MappingFieldComparator();

        when:
        comparator.compareLeafFields(searchIndexMapping as Map<String, Object>, applicationMapping as Map<String, Object>)

        then:
        thrown(Exception)

        where:
        searchIndexMapping | applicationMapping
        null               | null
        emptyMap()         | null
        null               | emptyMap()
    }

    def "compareLeafFields. The same fields"() {
        given:
        MappingFieldComparator comparator = new MappingFieldComparator()

        expect:
        comparator.compareLeafFields(searchIndexMapping, applicationMapping) == result

        where:
        searchIndexMapping                                           | applicationMapping                                            | result
        Map.of("key1", "value1", "key2", "value2", "key3", "value3") | Map.of("key1", "value1", "key2", "value2", "key3", "value3")  | EQUAL
        Map.of("key1", "value1", "key2", "value2")                   | Map.of("key1", "value1", "key2", "value2", "key3", "value3")  | NOT_COMPATIBLE
        Map.of("key1", "value1", "key2", "value2", "key3", "value3") | Map.of("key1", "value1", "key2", "value2")                    | NOT_COMPATIBLE
        Map.of("key1", "value1", "key2", "value2", "key3", "value3") | Map.of("key1", "value1", "key2", "value2", "key3", "value3+") | NOT_COMPATIBLE
    }

}
