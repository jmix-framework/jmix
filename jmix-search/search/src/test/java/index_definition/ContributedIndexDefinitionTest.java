/*
 * Copyright 2026 Haulmont.
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
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.ContributedIndexDefinitionTestConfiguration;
import test_support.TestJsonUtils;
import test_support.entity.TestReferenceEntity;
import test_support.entity.TestSimpleRootEntity;
import test_support.index_definition.IndexConfigurationMatcher;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ContributedIndexDefinitionTestConfiguration.class})
public class ContributedIndexDefinitionTest {

    @Autowired
    IndexConfigurationManager indexConfigurationManager;

    @Test
    void contributedDefinitionCreatesConfigurationForEntityWithoutJavaDefinition() {
        IndexConfiguration configuration = indexConfigurationManager.getIndexConfigurationByEntityName("test_ReferenceEntity");

        JsonNode expectedMapping = TestJsonUtils.readJsonFromFile("index_definition/contributed/test_mapping_contributed_only");
        MatcherAssert.assertThat(configuration, IndexConfigurationMatcher.configureWith(
                "test_ReferenceEntity", "search_index_test_referenceentity", TestReferenceEntity.class, expectedMapping));
    }

    @Test
    void contributedFieldsAreAppendedToJavaDefinition() {
        IndexConfiguration configuration = indexConfigurationManager.getIndexConfigurationByEntityName("test_SimpleRootEntity");

        JsonNode expectedMapping = TestJsonUtils.readJsonFromFile("index_definition/contributed/test_mapping_contributed_appended");
        MatcherAssert.assertThat(configuration, IndexConfigurationMatcher.configureWith(
                "test_SimpleRootEntity", "search_index_test_simplerootentity", TestSimpleRootEntity.class, expectedMapping));
    }

    @Test
    void contributedEntityIsDirectlyIndexed() {
        Assertions.assertTrue(indexConfigurationManager.isDirectlyIndexed("test_ReferenceEntity"));
        MatcherAssert.assertThat(indexConfigurationManager.getAllIndexedEntities(),
                Matchers.containsInAnyOrder("test_SimpleRootEntity", "test_ReferenceEntity"));
    }
}
