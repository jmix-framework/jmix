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

import com.fasterxml.jackson.databind.JsonNode;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.processor.impl.AnnotatedIndexDefinitionProcessor;
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
import test_support.IndexDefinitionProcessingTestConfiguration;
import test_support.TestJsonUtils;
import test_support.index_definition.IndexConfigurationMatcher;

import java.util.stream.Stream;

import static index_definition.AnnotatedIndexDefinitionTestCaseProvider.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {IndexDefinitionProcessingTestConfiguration.class}
)
public class AnnotatedIndexDefinitionProcessorTest {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedIndexDefinitionProcessorTest.class);

    @Autowired
    AnnotatedIndexDefinitionProcessor indexDefinitionProcessor;

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestCases")
    public void indexDefinitionProcessing(AnnotatedIndexDefinitionProcessorTestCase testCase) {
        log.info("Start Test Case '{}'", testCase);
        IndexConfiguration indexConfiguration = indexDefinitionProcessor.createIndexConfiguration(testCase.getIndexDefinitionClass().getName());
        Matcher<IndexConfiguration> matcher = createIndexConfigurationMatcher(testCase);
        MatcherAssert.assertThat(indexConfiguration, matcher);
    }

    protected static Stream<Arguments> provideTestCases() {
        return Stream.of(
                provideCommonTestCases(),
                provideReferenceTestCases(),
                provideFileTestCases(),
                provideEmbeddedTestCases()
        ).flatMap(a -> a);
    }

    protected Matcher<IndexConfiguration> createIndexConfigurationMatcher(AnnotatedIndexDefinitionProcessorTestCase testCase) {
        JsonNode expectedMapping = TestJsonUtils.readJsonFromFile(testCase.getPathToFileWithExpectedMapping());
        return IndexConfigurationMatcher.configureWith(
                testCase.getExpectedEntityName(),
                testCase.getExpectedIndexName(),
                testCase.getExpectedEntityClass(),
                expectedMapping
        );
    }
}
