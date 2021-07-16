/*
 * Copyright 2021 Haulmont.
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

package index_definition;

import org.junit.jupiter.params.provider.Arguments;
import test_support.entity.TestSimpleFileRootEntity;
import test_support.entity.TestRootEntity;
import test_support.entity.TestSimpleEmbeddedRootEntity;
import test_support.entity.TestSimpleRootEntity;
import test_support.index_definition.common.*;
import test_support.index_definition.embedded.TestIncludeAllEmbeddablePropertiesIndexDefinition;
import test_support.index_definition.embedded.TestIncludeEmbeddedPropertyIndexDefinition;
import test_support.index_definition.embedded.TestIncludeSpecificEmbeddablePropertyIndexDefinition;
import test_support.index_definition.file.TestIncludeLocalFilePropertyIndexDefinition;
import test_support.index_definition.file.TestIncludeLocalFilePropertyWithoutContentIndexDefinition;
import test_support.index_definition.reference.*;

import java.util.stream.Stream;

public class AnnotatedIndexDefinitionTestCaseProvider {

    public static Stream<Arguments> provideCommonTestCases() {
        return Stream.of(
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Wildcard includes all supported local properties")
                        .indexDefinitionClass(TestIncludeAllLocalPropertiesIndexDefinition.class)
                        .expectedEntityName("test_SimpleRootEntity")
                        .expectedIndexName("search_index_test_simplerootentity")
                        .expectedEntityClass(TestSimpleRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/common/test_mapping_include_all_local_properties")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Specified properties are included to mapping if supported")
                        .indexDefinitionClass(TestIncludeSpecificLocalPropertiesIndexDefinition.class)
                        .expectedEntityName("test_SimpleRootEntity")
                        .expectedIndexName("search_index_test_simplerootentity")
                        .expectedEntityClass(TestSimpleRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/common/test_mapping_include_specific_local_properties")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("It's possible to exclude some properties from wildcard coverage")
                        .indexDefinitionClass(TestIncludeAllLocalPropertiesWithExclusionIndexDefinition.class)
                        .expectedEntityName("test_SimpleRootEntity")
                        .expectedIndexName("search_index_test_simplerootentity")
                        .expectedEntityClass(TestSimpleRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/common/test_mapping_include_all_local_properties_with_exclusion")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Multiple annotation on a single method are supported")
                        .indexDefinitionClass(TestMultiAnnotationInclusionIndexDefinition.class)
                        .expectedEntityName("test_SimpleRootEntity")
                        .expectedIndexName("search_index_test_simplerootentity")
                        .expectedEntityClass(TestSimpleRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/common/test_mapping_multi_inclusion")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Multiple annotated methods are supported")
                        .indexDefinitionClass(TestMultiMethodInclusionIndexDefinition.class)
                        .expectedEntityName("test_SimpleRootEntity")
                        .expectedIndexName("search_index_test_simplerootentity")
                        .expectedEntityClass(TestSimpleRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/common/test_mapping_multi_inclusion")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Programmatic mapping is correct")
                        .indexDefinitionClass(TestProgrammaticMappingIndexDefinition.class)
                        .expectedEntityName("test_SimpleRootEntity")
                        .expectedIndexName("search_index_test_simplerootentity")
                        .expectedEntityClass(TestSimpleRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/common/test_mapping_programmatic")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Programmatic mapping ignores annotations")
                        .indexDefinitionClass(TestProgrammaticMappingWithAnnotationsIndexDefinition.class)
                        .expectedEntityName("test_SimpleRootEntity")
                        .expectedIndexName("search_index_test_simplerootentity")
                        .expectedEntityClass(TestSimpleRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/common/test_mapping_programmatic_with_annotations")
                        .build()
                )
        );
    }

    public static Stream<Arguments> provideReferenceTestCases() {
        return Stream.of(
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Inclusion of reference property produces instanceName mapping only")
                        .indexDefinitionClass(TestIncludeOneToOneReferencePropertyIndexDefinition.class)
                        .expectedEntityName("test_RootEntity")
                        .expectedIndexName("search_index_test_rootentity")
                        .expectedEntityClass(TestRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/reference/test_mapping_include_one_to_one_reference_property")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Nested wildcard includes all supported nested properties")
                        .indexDefinitionClass(TestIncludeAllOneToOneRefNestedPropertiesIndexDefinition.class)
                        .expectedEntityName("test_RootEntity")
                        .expectedIndexName("search_index_test_rootentity")
                        .expectedEntityClass(TestRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/reference/test_mapping_include_all_one_to_one_ref_nested_properties")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Specified nested properties are included to mapping if supported")
                        .indexDefinitionClass(TestIncludeSpecificOneToOneRefNestedPropertiesIndexDefinition.class)
                        .expectedEntityName("test_RootEntity")
                        .expectedIndexName("search_index_test_rootentity")
                        .expectedEntityClass(TestRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/reference/test_mapping_include_specific_one_to_one_ref_nested_properties")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Sub-nested wildcard includes all supported sub-nested properties")
                        .indexDefinitionClass(TestIncludeAllOneToOneSubRefNestedPropertiesIndexDefinition.class)
                        .expectedEntityName("test_RootEntity")
                        .expectedIndexName("search_index_test_rootentity")
                        .expectedEntityClass(TestRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/reference/test_mapping_include_all_one_to_one_sub_ref_nested_properties")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Specified sub-nested properties are included to mapping if supported")
                        .indexDefinitionClass(TestIncludeSpecificOneToOneSubRefNestedPropertiesIndexDefinition.class)
                        .expectedEntityName("test_RootEntity")
                        .expectedIndexName("search_index_test_rootentity")
                        .expectedEntityClass(TestRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/reference/test_mapping_include_specific_one_to_one_sub_ref_nested_properties")
                        .build()
                )
        );
    }

    public static Stream<Arguments> provideFileTestCases() {
        return Stream.of(
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("File property is mapped with fileName and content fields")
                        .indexDefinitionClass(TestIncludeLocalFilePropertyIndexDefinition.class)
                        .expectedEntityName("test_SimpleFileRootEntity")
                        .expectedIndexName("search_index_test_simplefilerootentity")
                        .expectedEntityClass(TestSimpleFileRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/file/test_mapping_include_local_file_property")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("File content field is still present in mapping with disabled content indexing")
                        .indexDefinitionClass(TestIncludeLocalFilePropertyWithoutContentIndexDefinition.class)
                        .expectedEntityName("test_SimpleFileRootEntity")
                        .expectedIndexName("search_index_test_simplefilerootentity")
                        .expectedEntityClass(TestSimpleFileRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/file/test_mapping_include_local_file_property")
                        .build()
                )
        );
    }

    public static Stream<Arguments> provideEmbeddedTestCases() {
        return Stream.of(
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Inclusion of embedded property includes all embeddable properties")
                        .indexDefinitionClass(TestIncludeEmbeddedPropertyIndexDefinition.class)
                        .expectedEntityName("test_SimpleEmbRootEntity")
                        .expectedIndexName("search_index_test_simpleembrootentity")
                        .expectedEntityClass(TestSimpleEmbeddedRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/embedded/test_mapping_include_all_embeddable_properties")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Embedded-level wildcard includes all supported embeddable properties")
                        .indexDefinitionClass(TestIncludeAllEmbeddablePropertiesIndexDefinition.class)
                        .expectedEntityName("test_SimpleEmbRootEntity")
                        .expectedIndexName("search_index_test_simpleembrootentity")
                        .expectedEntityClass(TestSimpleEmbeddedRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/embedded/test_mapping_include_all_embeddable_properties")
                        .build()
                ),
                Arguments.of(AnnotatedIndexDefinitionProcessorTestCase.builder("Specified embeddable properties are included to mapping if supported")
                        .indexDefinitionClass(TestIncludeSpecificEmbeddablePropertyIndexDefinition.class)
                        .expectedEntityName("test_SimpleEmbRootEntity")
                        .expectedIndexName("search_index_test_simpleembrootentity")
                        .expectedEntityClass(TestSimpleEmbeddedRootEntity.class)
                        .pathToFileWithExpectedMapping("index_definition/embedded/test_mapping_include_specific_embeddable_property")
                        .build()
                )
        );
    }
}
