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
import io.jmix.search.index.mapping.DynamicAttributesConfigurationGroup
import io.jmix.search.index.mapping.DynamicAttributesParameterKeys
import io.jmix.search.index.mapping.ParameterKeys
import io.jmix.search.index.mapping.strategy.impl.AutoMappingStrategy
import org.springframework.core.annotation.MergedAnnotation
import org.springframework.core.annotation.MergedAnnotations
import spock.lang.Specification

import java.util.stream.Collectors

import static java.util.Arrays.stream

class DynamicAttributesAnnotationParserTest extends Specification {

    public static final String SOME_ANALYZER = "ANotStandardAnalyzer"

    private static Set<DynamicAttributes> extractAnnotations(Class<?> indexDefinitionClass) {
        return stream(indexDefinitionClass.getMethods())
                .map(MergedAnnotations::from)
                .flatMap(MergedAnnotations::stream)
                .map(MergedAnnotation::synthesize)
                .filter(annotation -> annotation instanceof DynamicAttributes)
                .map(annotation -> (DynamicAttributes)annotation)
                .collect(Collectors.toSet());
    }    

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
        def annotations = extractAnnotations(IndexDefinitionSimple);
        DynamicAttributesConfigurationGroup definition = parser.createDefinition(annotations.iterator().next())

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
        def annotations = extractAnnotations(IndexDefinitionWithExcludes);
        DynamicAttributesConfigurationGroup definition = parser.createDefinition(annotations.iterator().next())

        then:
        definition.getExcludedCategories() == new String[]{"cat1", "cat2", "cat3"}
        definition.getExcludedProperties() == new String[]{"field1", "field2"}
    }

    def "CreateDefinition. parameters"() {
        given:
        def parser = new DynamicAttributesAnnotationParser()

        when:
        def annotations = extractAnnotations(IndexDefinitionWithParameters);
        Map<String, Object> parameters
                = parser.createDefinition(annotations.iterator().next()).getParameters()

        then:
        parameters.get(ParameterKeys.ANALYZER) == SOME_ANALYZER
        parameters.get(ParameterKeys.INDEX_FILE_CONTENT) == false
        parameters.get(DynamicAttributesParameterKeys.REFERENCE_FIELD_INDEXING_MODE)
                == ReferenceFieldsIndexingMode.NONE
    }

    def "extract from class. simple"() {
        given:
        def parser = new DynamicAttributesAnnotationParser()

        when:
        def annotations = extractAnnotations(aClass)

        then:
        annotations.size() == size

        where:
        aClass                               | size
        IndexDefinitionEmpty                 | 0
        IndexDefinitionEmptyToo              | 0
        IndexDefinitionSimple                | 1
        IndexDefinitionSimpleWithEmptyMethod | 1
        IndexDefinitionSomeOnOneMethod       | 2
        IndexDefinitionSomeInSomeMethods     | 5
    }

    def "extract from class. content check"() {
        given:
        def parser = new DynamicAttributesAnnotationParser()

        when:
        def annotations = extractAnnotations(IndexDefinitionSomeInSomeMethods)

        then:
        containsAnnotationsWithExcludedCategory(annotations, category)

        where:
        category << ["cat1", "cat2", "cat3", "cat4", "cat5"]
    }


    private static DynamicAttributes extractAnnotation(Class<?> aClass) {
        return aClass.getAnnotation(DynamicAttributes);

    }

    boolean containsAnnotationsWithExcludedCategory(Set<DynamicAttributes> annotations, String categoryToFind) {
        annotations
                .stream()
                .filter { annotation -> annotation.excludeCategories().size() == 1 }
                .filter { annotation -> (annotation.excludeCategories()[0] == categoryToFind) }
                .count() == 1
    }

    private interface IndexDefinitionWithExcludes {
        @DynamicAttributes(excludeCategories = ["cat1", "cat2", "cat3"], excludeFields = ["field1", "field2"])
        void method();

    }

    private interface IndexDefinitionWithParameters {
        @DynamicAttributes(
                referenceFieldsIndexingMode = ReferenceFieldsIndexingMode.NONE,
                analyzer = SOME_ANALYZER,
                indexFileContent = false
        )
        void method();

    }

    private interface IndexDefinitionSimple {
        @DynamicAttributes
        void method();

    }

    private interface IndexDefinitionEmpty {
        void method();
    }

    private interface IndexDefinitionEmptyToo {
        void method();
    }

    private interface IndexDefinitionSimpleWithEmptyMethod {
        @DynamicAttributes
        void method();

        void method2();
    }

    private interface IndexDefinitionSomeOnOneMethod {
        @DynamicAttributes
        @DynamicAttributes(excludeCategories = ["cat1", "cat2", "cat3"], excludeFields = ["field1", "field2"])
        void method();

        void method2();
    }

    private interface IndexDefinitionSomeInSomeMethods {
        @DynamicAttributes(excludeCategories = ["cat1"])
        @DynamicAttributes(excludeCategories = ["cat2"])
        void method();

        @DynamicAttributes(excludeCategories = ["cat3"])
        @DynamicAttributes(excludeCategories = ["cat4"])
        @DynamicAttributes(excludeCategories = ["cat5"])
        void method2();
    }

}
