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

package llm_beans;

import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.autoconfigure.aitools.AiToolsAutoConfiguration;
import io.jmix.autoconfigure.aitools.AiToolsDataLoadAutoConfiguration;
import io.jmix.autoconfigure.eclipselink.EclipselinkAutoConfiguration;
import io.jmix.autoconfigure.reports.ReportsLlmAutoConfiguration;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import io.jmix.reports.libintegration.LlmDataLoader;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmQueryExecutionRequest;
import io.jmix.reports.llm.LlmQueryExecutionResult;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The LLM data set beans exist only while the AI Tools data-load subsystem does.
 */
public class ReportsLlmAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class,
                    EclipselinkAutoConfiguration.class, CacheAutoConfiguration.class,
                    AiToolsAutoConfiguration.class, AiToolsDataLoadAutoConfiguration.class,
                    ReportsLlmAutoConfiguration.class))
            .withPropertyValues("spring.datasource.url=jdbc:hsqldb:mem:reportsllmtestdb",
                    "spring.datasource.username=sa", "spring.cache.type=simple")
            .withAllowBeanDefinitionOverriding(true)
            .withUserConfiguration(UserConfiguration.class);

    @Test
    void testLlmBeansPresentWhenTheAddOnIsOnBoard() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(LlmDataQueryService.class);
            assertThat(context).hasSingleBean(LlmDataLoader.class);
        });
    }

    @Test
    void testNoLlmBeansWhenTheDataLoadSubsystemIsDisabled() {
        contextRunner.withPropertyValues("jmix.aitools.dataload.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(LlmDataQueryService.class);
                    assertThat(context).doesNotHaveBean(LlmDataLoader.class);
                });
    }

    @Test
    void testNoLlmBeansWhenTheWholeAddOnIsDisabled() {
        contextRunner.withPropertyValues("jmix.aitools.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(LlmDataQueryService.class);
                    assertThat(context).doesNotHaveBean(LlmDataLoader.class);
                });
    }

    @Test
    void testNoLlmBeansWhenTheAddOnIsNotOnTheClasspath() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ReportsLlmAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(EntityDataLoadGenerationService.class))
                .withUserConfiguration(UserConfiguration.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(LlmDataQueryService.class);
                    assertThat(context).doesNotHaveBean(LlmDataLoader.class);
                });
    }

    @Test
    void testLoaderIsDeclaredForAServiceAnApplicationSubstitutes() {
        // The seam is meant to be replaceable, and a data set type the designer offers has to be one that runs.
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ReportsLlmAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(EntityDataLoadGenerationService.class))
                .withUserConfiguration(UserConfiguration.class, SubstitutedServiceConfiguration.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(LlmDataQueryService.class);
                    assertThat(context).hasSingleBean(LlmDataLoader.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class SubstitutedServiceConfiguration {

        @Bean
        LlmDataQueryService llmDataQueryService() {
            return new LlmDataQueryService() {

                @Override
                public boolean isGenerationAvailable() {
                    return true;
                }

                @Override
                public LlmDataQuery generate(LlmQueryGenerationRequest request) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<String> validate(LlmDataQuery query) {
                    return List.of();
                }

                @Override
                public LlmQueryExecutionResult execute(LlmQueryExecutionRequest request) {
                    return new LlmQueryExecutionResult(List.of(), false);
                }
            };
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class UserConfiguration {

        @Bean
        ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
            return new LocalApplicationEventChannelSupplier();
        }

        /**
         * Contributed by the Reports module scan in an application; the runner starts no module here.
         */
        @Bean
        LlmDataQuerySerializer llmDataQuerySerializer() {
            return new LlmDataQuerySerializer();
        }
    }
}
