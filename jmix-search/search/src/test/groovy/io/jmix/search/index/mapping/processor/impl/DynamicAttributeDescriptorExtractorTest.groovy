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

import io.jmix.search.index.DynamicAttributesIndexingDescriptor
import io.jmix.search.index.mapping.processor.impl.samples.*

import static io.jmix.search.index.annotation.ReferenceFieldsIndexingMode.*
import spock.lang.Specification


class DynamicAttributeDescriptorExtractorTest extends Specification {

    def "the annotation is absent"() {
        given:
        DynamicAttributeDescriptorExtractor extractor = new DynamicAttributeDescriptorExtractor()

        when:
        def extractedDescriptor = extractor.extract(Configuration)

        then:
        extractedDescriptor == null
    }

    def "regular extraction"() {
        given:
        DynamicAttributeDescriptorExtractor extractor = new DynamicAttributeDescriptorExtractor()

        and:
        DynamicAttributesIndexingDescriptor expectedDescriptor = new DynamicAttributesIndexingDescriptor(
                isOn,
                referenceMode,
                excludedCategories,
                excludedFields,
                analizer,
                fileContentIndexing)

        when:
        def extractedDescriptor = extractor.extract(descriptorClass)

        then:
        expectedDescriptor == extractedDescriptor

        where:
        descriptorClass | isOn | referenceMode      | excludedCategories | excludedFields       | analizer   | fileContentIndexing
        Configuration2  | true | INSTANCE_NAME_ONLY | []                 | []                   | ""         | true
        Configuration3  | true | INSTANCE_NAME_ONLY | ["ex1", "ex2"]     | []                   | ""         | true
        Configuration4  | true | INSTANCE_NAME_ONLY | ["ex1", "ex2"]     | ["field3", "field4"] | ""         | true
        Configuration5  | true | NONE               | ["ex1", "ex2"]     | ["field3", "field4"] | "standard" | true
        Configuration6  | true | NONE               | ["ex1", "ex2"]     | ["field3", "field4"] | "standard" | false
    }

    def "duplicates cleaning"() {
        given:
        DynamicAttributeDescriptorExtractor extractor = new DynamicAttributeDescriptorExtractor()

        when:
        def descriptor = extractor.extract(Configuration9)

        then:
        descriptor.excludedCategories() == ["c4", "c3"]
        descriptor.excludedFields() == ["f4", "f3"]
    }
}
