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
import io.jmix.search.index.mapping.processor.impl.samples.Configuration3

import io.jmix.search.index.mapping.processor.impl.samples.Configuration4
import io.jmix.search.index.mapping.processor.impl.samples.Configuration5
import io.jmix.search.index.mapping.processor.impl.samples.Configuration6
import io.jmix.search.index.mapping.processor.impl.samples.Configuration7
import io.jmix.search.index.mapping.processor.impl.samples.Configuration8
import io.jmix.search.index.mapping.processor.impl.samples.Configuration9

import static io.jmix.search.index.annotation.ReferenceFieldsIndexingMode.*
import io.jmix.search.index.mapping.processor.impl.samples.Configuration
import io.jmix.search.index.mapping.processor.impl.samples.Configuration2
import spock.lang.Specification


class DynamicAttributeDescriptorExtractorTest extends Specification {
    def "regular extraction"() {
        given:
        DynamicAttributeDescriptorExtractor extractor = new DynamicAttributeDescriptorExtractor()

        and:
        DynamicAttributesIndexingDescriptor expectedDescriptor = new DynamicAttributesIndexingDescriptor(
                isOn,
                referenceMode,
                includedCategories,
                excludedCategories,
                includedFields,
                excludedFields)

        when:
        def extractedDescriptor = extractor.extract(descriptorClass)


        then:
        expectedDescriptor == extractedDescriptor

        where:
        descriptorClass | isOn  | referenceMode      | includedCategories  | excludedCategories | includedFields       | excludedFields
        Configuration   | false | NONE               | []                  | []                 | []                   | []
        Configuration2  | true  | INSTANCE_NAME_ONLY | []                  | []                 | []                   | []
        Configuration3  | true  | INSTANCE_NAME_ONLY | ["category1", "c2"] | ["ex1", "ex2"]     | []                   | []
        Configuration4  | true  | INSTANCE_NAME_ONLY | ["category1", "c2"] | ["ex1", "ex2"]     | ["field1", "field2"] | ["field3", "field4"]
        Configuration5  | true  | NONE               | ["category1", "c2"] | ["ex1", "ex2"]     | ["field1", "field2"] | ["field3", "field4"]
    }

    def "wrong configuration"() {
        given:
        DynamicAttributeDescriptorExtractor extractor = new DynamicAttributeDescriptorExtractor()

        when:
        extractor.extract(descriptorClass)

        then:
        def exception = thrown(DynamicAttributesIndexingConfigurationException)
        exception.getMessage().contains(includedPartsText)
        exception.getMessage().contains(excludedPartsText)
        exception.getMessage().contains(partText)

        where:
        descriptorClass | includedPartsText | excludedPartsText | partText
        Configuration6  | "c1, c2"          | "c2, c3"          | "Categories"
        Configuration7  | "c1, c2"          | "c2, c3"          | "Categories"
        Configuration8  | "f1, f2"          | "f1, f3"          | "Fields"
    }

    def "clean"() {
        given:
        DynamicAttributeDescriptorExtractor extractor = new DynamicAttributeDescriptorExtractor()

        when:
        def descriptor = extractor.extract(Configuration9)

        then:
        descriptor.includedCategories() == ["c2", "c1", "c5"]
        descriptor.excludedCategories() == ["c4", "c3"]
        descriptor.includedFields() == ["f2", "f1", "f5"]
        descriptor.excludedFields() == ["f4", "f3"]
    }


}
