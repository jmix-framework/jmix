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

package io.jmix.search.index.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.jmix.search.index.mapping.propertyvalue.impl.FilePropertyValueExtractor
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy
import io.jmix.search.index.mapping.strategy.impl.AutoMappingStrategy
import spock.lang.Specification

import static io.jmix.search.index.annotation.ReferenceFieldsIndexingMode.*
import static io.jmix.search.index.mapping.DynamicAttributesParameterKeys.REFERENCE_FIELD_INDEXING_MODE
import static io.jmix.search.index.mapping.ParameterKeys.ANALYZER
import static io.jmix.search.index.mapping.ParameterKeys.INDEX_FILE_CONTENT

class DynamicAttributesConfigurationGroupTest extends Specification {
    private static final String[] EXCLUDED_CATEGORIES = ["category1", "category2", "category3"]
    private static final String[] EXCLUDED_PROPERTIES = ["field1", "field2", "field3"]
    public static final String SOME_ANALYZER = "someNotStandardAnalyzer"
    public static final String SAMPLE_JSON_TEXT = "{\"parameter\": \"value\"}"
    public static final String SAMPLE_JSON_TEXT_2 = "{\"parameter\": \"value 2\"}"

    def "building test"() {
        given:
        def propertyValueExtractorMock = Mock(FilePropertyValueExtractor)

        when:
        def dynamicAttributesGroup = DynamicAttributesConfigurationGroup.builder()
                .excludeCategories(EXCLUDED_CATEGORIES)
                .excludeProperties(EXCLUDED_PROPERTIES)
                .withParameters(Map.of(REFERENCE_FIELD_INDEXING_MODE, NONE))
                .addParameter(INDEX_FILE_CONTENT, true)
                .addParameter(ANALYZER, SOME_ANALYZER)
                .withFieldMappingStrategyClass(AutoMappingStrategy)
                .withPropertyValueExtractor(propertyValueExtractorMock)
                .build()

        then:
        dynamicAttributesGroup.getExcludedProperties() == EXCLUDED_PROPERTIES
        dynamicAttributesGroup.getExcludedCategories() == EXCLUDED_CATEGORIES
        dynamicAttributesGroup.getParameters() == Map.of(
                REFERENCE_FIELD_INDEXING_MODE, NONE,
                INDEX_FILE_CONTENT, true,
                ANALYZER, SOME_ANALYZER)
        dynamicAttributesGroup.getPropertyValueExtractor() == propertyValueExtractorMock
        dynamicAttributesGroup.getFieldMappingStrategyClass() == AutoMappingStrategy

    }

    def "strategy and class together don't fail"() {
        given:
        def propertyValueExtractorMock = Mock(FilePropertyValueExtractor)


        def fieldMappingStrategyMock = Mock(FieldMappingStrategy)
        when:
        def dynamicAttributesGroup = DynamicAttributesConfigurationGroup.builder()
                .excludeCategories(EXCLUDED_CATEGORIES)
                .excludeProperties(EXCLUDED_PROPERTIES)
                .withParameters(Map.of(REFERENCE_FIELD_INDEXING_MODE, NONE))
                .addParameter(INDEX_FILE_CONTENT, true)
                .addParameter(ANALYZER, SOME_ANALYZER)
                .withFieldMappingStrategy(fieldMappingStrategyMock)
                .withFieldMappingStrategyClass(AutoMappingStrategy)
                .withPropertyValueExtractor(propertyValueExtractorMock)
                .build()

        then:
        dynamicAttributesGroup.getFieldMappingStrategy() == fieldMappingStrategyMock
        dynamicAttributesGroup.getFieldMappingStrategyClass() == AutoMappingStrategy
    }

    def "with field configuration. String"() {
        given:
        def propertyValueExtractorMock = Mock(FilePropertyValueExtractor)
        def fieldMappingStrategyMock = Mock(FieldMappingStrategy)

        and:
        FieldConfiguration fieldConfiguration = createFieldConfiguration(SAMPLE_JSON_TEXT);

        when:
        def dynamicAttributesGroup = DynamicAttributesConfigurationGroup.builder()
                .excludeCategories(EXCLUDED_CATEGORIES)
                .excludeProperties(EXCLUDED_PROPERTIES)
                .withParameters(Map.of(REFERENCE_FIELD_INDEXING_MODE, NONE))
                .addParameter(INDEX_FILE_CONTENT, true)
                .addParameter(ANALYZER, SOME_ANALYZER)
                .withFieldMappingStrategy(fieldMappingStrategyMock)
                .withFieldMappingStrategyClass(AutoMappingStrategy)
                .withPropertyValueExtractor(propertyValueExtractorMock)
                .withFieldConfiguration(SAMPLE_JSON_TEXT)
                .build()

        then:
        dynamicAttributesGroup.getFieldConfiguration().asJson() == fieldConfiguration.asJson()
    }

    def "with field configuration. ObjectNode"() {
        given:
        def propertyValueExtractorMock = Mock(FilePropertyValueExtractor)
        def fieldMappingStrategyMock = Mock(FieldMappingStrategy)

        and:
        ObjectNode fieldConfigurationNode = asNode(SAMPLE_JSON_TEXT);

        when:
        def dynamicAttributesGroup = DynamicAttributesConfigurationGroup.builder()
                .excludeCategories(EXCLUDED_CATEGORIES)
                .excludeProperties(EXCLUDED_PROPERTIES)
                .withParameters(Map.of(REFERENCE_FIELD_INDEXING_MODE, NONE))
                .addParameter(INDEX_FILE_CONTENT, true)
                .addParameter(ANALYZER, SOME_ANALYZER)
                .withFieldMappingStrategy(fieldMappingStrategyMock)
                .withFieldMappingStrategyClass(AutoMappingStrategy)
                .withPropertyValueExtractor(propertyValueExtractorMock)
                .withFieldConfiguration(SAMPLE_JSON_TEXT)
                .build()

        then:
        dynamicAttributesGroup.getFieldConfiguration().asJson() == fieldConfigurationNode
    }

    def "with field configuration. Both methods with String and ObjectNode types"() {
        given:
        def propertyValueExtractorMock = Mock(FilePropertyValueExtractor)
        def fieldMappingStrategyMock = Mock(FieldMappingStrategy)

        and:
        ObjectNode fieldConfigurationNode = asNode(SAMPLE_JSON_TEXT_2);
        FieldConfiguration fieldConfiguration = createFieldConfiguration(SAMPLE_JSON_TEXT);
        when:
        def dynamicAttributesGroup = DynamicAttributesConfigurationGroup.builder()
                .excludeCategories(EXCLUDED_CATEGORIES)
                .excludeProperties(EXCLUDED_PROPERTIES)
                .withParameters(Map.of(REFERENCE_FIELD_INDEXING_MODE, NONE))
                .addParameter(INDEX_FILE_CONTENT, true)
                .addParameter(ANALYZER, SOME_ANALYZER)
                .withFieldMappingStrategy(fieldMappingStrategyMock)
                .withFieldMappingStrategyClass(AutoMappingStrategy)
                .withPropertyValueExtractor(propertyValueExtractorMock)
                .withFieldConfiguration(fieldConfigurationNode)
                .withFieldConfiguration(SAMPLE_JSON_TEXT)
                .build()


        then:
        dynamicAttributesGroup.getFieldConfiguration().asJson() == fieldConfiguration.asJson()
    }

    private static FieldConfiguration createFieldConfiguration(String s) {
        return FieldConfiguration.create(new ObjectMapper().readValue(s, ObjectNode));
    }

    private static ObjectNode asNode(String s) {
        return new ObjectMapper().readValue(s, ObjectNode);
    }
}
