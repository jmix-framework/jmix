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

package settings;

import org.junit.jupiter.params.provider.Arguments;
import test_support.settings.analysis.*;

import java.util.stream.Stream;

public class SettingsConfigurationTestCaseProvider {

    public static Stream<Arguments> provideAnalysisTestCases() {
        return Stream.of(
                Arguments.of(SettingsConfigurationTestCase.builder("Modified built-in analyzer")
                        .indexDefinitionClass(ModifiedBuiltInAnalyzerIndexDefinition.class)
                        .pathToFileWithExpectedSettings("settings/analysis/modified_build_in_analyzer_content")
                        .build()),
                Arguments.of(SettingsConfigurationTestCase.builder("Custom analyzer with built-in elements")
                        .indexDefinitionClass(CustomAnalyzerWithBuiltInElementsIndexDefinition.class)
                        .pathToFileWithExpectedSettings("settings/analysis/custom_analyzer_build_in_elements_content")
                        .build()),
                Arguments.of(SettingsConfigurationTestCase.builder("Custom analyzer with custom elements")
                        .indexDefinitionClass(CustomAnalyzerWithCustomElementsIndexDefinition.class)
                        .pathToFileWithExpectedSettings("settings/analysis/custom_analyzer_custom_elements_content")
                        .build()),
                Arguments.of(SettingsConfigurationTestCase.builder("Native modified built-in analyzer")
                        .indexDefinitionClass(NativeModifiedBuiltInAnalyzerIndexDefinition.class)
                        .pathToFileWithExpectedSettings("settings/analysis/native_modified_build_in_analyzer_content")
                        .build()),
                Arguments.of(SettingsConfigurationTestCase.builder("Native custom analyzer with built-in elements")
                        .indexDefinitionClass(NativeCustomAnalyzerWithBuiltInElementsIndexDefinition.class)
                        .pathToFileWithExpectedSettings("settings/analysis/native_custom_analyzer_build_in_elements_content")
                        .build()),
                Arguments.of(SettingsConfigurationTestCase.builder("Native custom analyzer with custom elements")
                        .indexDefinitionClass(NativeCustomAnalyzerWithCustomElementsIndexDefinition.class)
                        .pathToFileWithExpectedSettings("settings/analysis/native_custom_analyzer_custom_elements_content")
                        .build()),
                Arguments.of(SettingsConfigurationTestCase.builder("No analyzers")
                        .indexDefinitionClass(NoAnalysisElementsIndexDefinition.class)
                        .pathToFileWithExpectedSettings("settings/analysis/not_analyzers_content")
                        .build()),
                Arguments.of(SettingsConfigurationTestCase.builder("Multiple analyzers")
                        .indexDefinitionClass(MultipleAnalyzersIndexDefinition.class)
                        .pathToFileWithExpectedSettings("settings/analysis/multiple_analyzers_content")
                        .build())
        );
    }
}
