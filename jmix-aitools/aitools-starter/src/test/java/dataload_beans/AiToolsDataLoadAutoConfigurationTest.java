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

package dataload_beans;

import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.dataload.AiDataLoadService;
import io.jmix.aitools.dataload.introspection.AvailableEntityFilter;
import io.jmix.aitools.dataload.introspection.JpaDomainModelIntrospector;
import io.jmix.aitools.dataload.tool.JpqlExecutorTool;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.autoconfigure.aitools.AiToolsAutoConfiguration;
import io.jmix.autoconfigure.aitools.AiToolsDataLoadAutoConfiguration;
import io.jmix.autoconfigure.eclipselink.EclipselinkAutoConfiguration;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class AiToolsDataLoadAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class,
                    EclipselinkAutoConfiguration.class, CacheAutoConfiguration.class,
                    AiToolsAutoConfiguration.class, AiToolsDataLoadAutoConfiguration.class))
            .withPropertyValues("spring.datasource.url=jdbc:hsqldb:mem:testdb", "spring.datasource.username=sa",
                    "spring.cache.type=simple")
            .withAllowBeanDefinitionOverriding(true)
            .withUserConfiguration(UserConfiguration.class);

    @Test
    void testDataLoadBeansPresentByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(AiDataLoadService.class);
            assertThat(context).hasSingleBean(JpaDomainModelIntrospector.class);
            assertThat(context).hasSingleBean(JpqlValidationService.class);
            assertThat(context).hasSingleBean(JpqlExecutorTool.class);
            assertThat(context).hasSingleBean(AvailableEntityFilter.class);
        });
    }

    @Test
    void testDataLoadDisabledRemovesWholeSubsystem() {
        contextRunner.withPropertyValues("jmix.aitools.dataload.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(AiDataLoadService.class);
                    assertThat(context).doesNotHaveBean(JpaDomainModelIntrospector.class);
                    assertThat(context).doesNotHaveBean(JpqlValidationService.class);
                    assertThat(context).doesNotHaveBean(JpqlExecutorTool.class);
                    assertThat(context).doesNotHaveBean(AvailableEntityFilter.class);
                    assertThat(context).hasSingleBean(AiToolRegistry.class);
                    assertThat(context).hasSingleBean(ChatClientFactory.class);
                });
    }

    @Test
    void testAiToolsDisabledRemovesDataLoadToo() {
        contextRunner.withPropertyValues("jmix.aitools.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(AiToolRegistry.class);
                    assertThat(context).doesNotHaveBean(AiDataLoadService.class);
                    assertThat(context).doesNotHaveBean(JpaDomainModelIntrospector.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class UserConfiguration {

        @Bean
        ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier() {
            return new LocalApplicationEventChannelSupplier();
        }
    }
}
