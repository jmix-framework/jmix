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

package io.jmix.search.index.mapping.processor.impl.dynattr

import io.jmix.search.index.annotation.DynamicAttributes
import io.jmix.search.index.annotation.ReferenceFieldsIndexingMode
import io.jmix.search.index.mapping.DynamicAttributesGroup
import io.jmix.search.index.mapping.DynamicAttributesParameterKeys
import io.jmix.search.index.mapping.ParameterKeys
import io.jmix.search.index.mapping.strategy.impl.AutoMappingStrategy
import spock.lang.Specification

class DynamicAttributesAnnotationParserTest extends Specification {


    public static final String SOME_ANALYZER = "ANotStandardAnalyzer"

    def "CreateDefinition. null check"() {
        given:
        def parser = new DynamicAttributesAnnotationParser()

        when:
        parser.createDefinition(null)

        then:
        thrown(NullPointerException)
    }


    def "CreateDefinition. Annotation without parameters"() {
        given:
        def parser = new DynamicAttributesAnnotationParser()

        when:
        DynamicAttributesGroup definition = parser.createDefinition(extractAnnotation(IndexDefinitionSimple))

        then:
        definition.getFieldMappingStrategyClass() == AutoMappingStrategy
        definition.getFieldMappingStrategy() == null
        definition.getExcludedCategories() == new String[0]
        definition.getExcludedProperties() == new String[0]
        definition.getFieldConfiguration() == null
        definition.getPropertyValueExtractor() == null
        definition.getParameters() == Map.of(
                DynamicAttributesParameterKeys.REFERENCE_FIELD_INDEXING_MODE,
                ReferenceFieldsIndexingMode.INSTANCE_NAME_ONLY,

                ParameterKeys.INDEX_FILE_CONTENT, true
        )
    }

    def "CreateDefinition. Excludes"() {
        given:
        def parser = new DynamicAttributesAnnotationParser()

        when:
        DynamicAttributesGroup definition = parser.createDefinition(extractAnnotation(IndexDefinitionWithExcludes))

        then:
        definition.getExcludedCategories() == new String[]{"cat1", "cat2", "cat3"}
        definition.getExcludedProperties() == new String[]{"field1", "field2"}
    }

    def "CreateDefinition. parameters"() {
        given:
        def parser = new DynamicAttributesAnnotationParser()

        when:
        Map<String, Object> parameters
                = parser.createDefinition(extractAnnotation(IndexDefinitionWithParameters)).getParameters()

        then:
        parameters.get(ParameterKeys.ANALYZER) == SOME_ANALYZER
        parameters.get(ParameterKeys.INDEX_FILE_CONTENT) == false
        parameters.get(DynamicAttributesParameterKeys.REFERENCE_FIELD_INDEXING_MODE)
                == ReferenceFieldsIndexingMode.NONE
    }


    private static DynamicAttributes extractAnnotation(Class<?> aClass) {
        return aClass.getAnnotation(DynamicAttributes);

    }

    @DynamicAttributes
    private static class IndexDefinitionSimple {

    }

    @DynamicAttributes(excludeCategories = ["cat1", "cat2", "cat3"], excludeFields = ["field1", "field2"])
    private static class IndexDefinitionWithExcludes {

    }

    @DynamicAttributes(
            referenceFieldsIndexingMode = ReferenceFieldsIndexingMode.NONE,
            analyzer = SOME_ANALYZER,
            indexFileContent = false
    )
    private static class IndexDefinitionWithParameters {

    }

}
