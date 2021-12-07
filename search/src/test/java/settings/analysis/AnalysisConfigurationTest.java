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

package settings.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import index_definition.AnnotatedIndexDefinitionProcessorTest;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.processor.impl.AnnotatedIndexDefinitionProcessor;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import settings.SettingsConfigurationTestCase;
import test_support.AnalysisConfigurationTestConfiguration;
import test_support.TestJsonUtils;
import test_support.settings.analysis.SettingsMatcher;

import java.util.stream.Stream;

import static settings.SettingsConfigurationTestCaseProvider.provideAnalysisTestCases;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {AnalysisConfigurationTestConfiguration.class}
)
public class AnalysisConfigurationTest {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedIndexDefinitionProcessorTest.class);

    @Autowired
    AnnotatedIndexDefinitionProcessor indexDefinitionProcessor;

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestCases")
    public void indexDefinitionProcessing(SettingsConfigurationTestCase testCase) {
        log.info("Start Test Case '{}'", testCase);
        IndexConfiguration indexConfiguration = indexDefinitionProcessor.createIndexConfiguration(testCase.getIndexDefinitionClass().getName());
        Matcher<Settings> matcher = createSettingsMatcher(testCase);
        MatcherAssert.assertThat(indexConfiguration.getSettings(), matcher);
    }

    protected static Stream<Arguments> provideTestCases() {
        return Stream.of(
                provideAnalysisTestCases()
        ).flatMap(a -> a);
    }

    protected Matcher<Settings> createSettingsMatcher(SettingsConfigurationTestCase testCase) {
        JsonNode settingsNode = TestJsonUtils.readJsonFromFile(testCase.getPathToFileWithExpectedSettings());
        Settings settings = Settings.builder().loadFromSource(settingsNode.toString(), XContentType.JSON).build();
        return SettingsMatcher.configureWith(settings);
    }
}
