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

package io.jmix.search.index.mapping.processor.impl

import spock.lang.Specification

import static io.jmix.search.index.mapping.processor.impl.DynamicAttributesIndexingConfigurationException.ConflictType.CATEGORIES
import static io.jmix.search.index.mapping.processor.impl.DynamicAttributesIndexingConfigurationException.ConflictType.FIELDS

class DynamicAttributesIndexingConfigurationExceptionTest extends Specification {

    private static final String MESSAGE_1 = "Index configuration can't be parsed. " +
            "The 'includedCategories' parameter value 'abc' conflicts with " +
            "the 'excludedCategories' parameter value 'abc, def'."

    private static final String MESSAGE_2 = "Index configuration can't be parsed. " +
            "The 'includedCategories' parameter value 'abc' conflicts with " +
            "the 'excludedCategories' parameter value 'abc'."

    private static final String MESSAGE_3 = "Index configuration can't be parsed. " +
            "The 'includedCategories' parameter value 'c1, c2' conflicts with " +
            "the 'excludedCategories' parameter value 'c2, c3'."

    private static final String MESSAGE_4 = "Index configuration can't be parsed. " +
            "The 'includedFields' parameter value 'abc' conflicts with " +
            "the 'excludedFields' parameter value 'abc, def'."

    private static final String MESSAGE_5 = "Index configuration can't be parsed. " +
            "The 'includedFields' parameter value 'abc' conflicts with " +
            "the 'excludedFields' parameter value 'abc'."

    private static final String MESSAGE_6 = "Index configuration can't be parsed. " +
            "The 'includedFields' parameter value 'c1, c2' conflicts with " +
            "the 'excludedFields' parameter value 'c2, c3'."

    def "GetMessage"() {
        given:
        def exception = new DynamicAttributesIndexingConfigurationException(conflictType, includedParts, excludedParts)

        expect:
        exception.getMessage() == message

        where:
        conflictType | includedParts | excludedParts  || message
        CATEGORIES   | ["abc"]       | ["abc", "def"] || MESSAGE_1
        CATEGORIES   | ["abc"]       | ["abc"]        || MESSAGE_2
        CATEGORIES   | ["c1", "c2"]  | ["c2", "c3"]   || MESSAGE_3
        FIELDS       | ["abc"]       | ["abc", "def"] || MESSAGE_4
        FIELDS       | ["abc"]       | ["abc"]        || MESSAGE_5
        FIELDS       | ["c1", "c2"]  | ["c2", "c3"]   || MESSAGE_6
    }
}
