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

package test_support;

import io.jmix.search.index.mapping.ContributedIndexDefinition;
import io.jmix.search.index.mapping.ExtendedSearchSettings;
import io.jmix.search.index.mapping.IndexDefinitionContributor;
import io.jmix.search.index.mapping.MappingDefinition;
import io.jmix.search.index.mapping.StaticAttributesGroupConfiguration;
import io.jmix.search.index.mapping.strategy.impl.AutoMappingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import test_support.entity.TestSubReferenceEntity;
import test_support.index_definition.common.TestIncludeSpecificLocalPropertiesIndexDefinition;

import java.util.List;

@Configuration
@Import({IndexDefinitionProcessingTestConfiguration.class})
public class ContributedIndexDefinitionTestConfiguration {

    @Bean
    public TestAutoDetectableIndexDefinitionScope testAutoDetectableIndexDefinitionScope() {
        return TestAutoDetectableIndexDefinitionScope.builder()
                .classes(TestIncludeSpecificLocalPropertiesIndexDefinition.class)
                .build();
    }

    @Bean
    public IndexDefinitionContributor testIndexDefinitionContributor() {
        return () -> List.of(
                ContributedIndexDefinition.builder("test_SimpleRootEntity", mapping("secondTextValue"))
                        // Ignored: the entity has a Java definition, which keeps its own settings.
                        .withExtendedSearchSettings(extendedSearchSettings())
                        .withIndexablePredicate(instance -> false)
                        .build(),
                ContributedIndexDefinition.builder("test_ReferenceEntity", mapping("textValue")).build(),
                ContributedIndexDefinition.builder("test_SubReferenceEntity", mapping("textValue"))
                        .withExtendedSearchSettings(extendedSearchSettings())
                        .withIndexablePredicate(instance -> "indexed".equals(((TestSubReferenceEntity) instance).getName()))
                        .build());
    }

    private static ExtendedSearchSettings extendedSearchSettings() {
        return ExtendedSearchSettings.builder().build();
    }

    private static MappingDefinition mapping(String... properties) {
        return MappingDefinition.builder()
                .addStaticAttributesGroup(StaticAttributesGroupConfiguration.builder()
                        .includeProperties(properties)
                        .withFieldMappingStrategyClass(AutoMappingStrategy.class)
                        .build())
                .build();
    }
}
