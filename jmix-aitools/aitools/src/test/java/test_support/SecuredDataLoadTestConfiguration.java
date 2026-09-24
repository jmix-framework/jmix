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

import io.jmix.aitools.AiToolsConfiguration;
import io.jmix.aitools.AiToolsDataLoadConfiguration;
import io.jmix.aitools.dataload.introspection.AvailableEntityFilter;
import io.jmix.aitools.dataload.introspection.JpaDomainModelIntrospector;
import io.jmix.aitools.dataload.introspection.impl.DefaultAvailableEntityFilter;
import io.jmix.aitools.dataload.prompt.DataLoadChatSystemPromptProvider;
import io.jmix.aitools.dataload.prompt.impl.DefaultDataLoadChatSystemPromptProvider;
import io.jmix.aitools.service.prompt.AiAssistantSystemPromptProvider;
import io.jmix.aitools.service.prompt.impl.DefaultAiAssistantSystemPromptProvider;
import io.jmix.aitools.tool.AiToolDescriptorProvider;
import io.jmix.aitools.tool.impl.AiToolDescriptorProviderImpl;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.annotation.MessageSourceBasenames;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.security.SecurityConfiguration;
import io.jmix.securitydata.SecurityDataConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.HsqlEmbeddedDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import repair.test_support.StubChatModel;

/**
 * Test context for the data-load subsystem with real persistence (embedded HSQL) and real security
 * ({@code security-data}): resource and row-level roles are enforced exactly as in an application.
 */
@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class,
        SecurityConfiguration.class, SecurityDataConfiguration.class,
        AiToolsConfiguration.class, AiToolsDataLoadConfiguration.class,
        CommonCoreTestConfiguration.class, HsqlEmbeddedDataSourceTestConfiguration.class,
        JpaMainStoreTestConfiguration.class, SecuredDataLoadTestSupport.class})
@JmixModule(dependsOn = {SecurityDataConfiguration.class, EclipselinkConfiguration.class,
        AiToolsConfiguration.class})
@MessageSourceBasenames("test_support/messages")
public class SecuredDataLoadTestConfiguration {

    @Bean("test_JpaDomainModelIntrospector")
    public JpaDomainModelIntrospector jpaDomainModelIntrospector() {
        return new JpaDomainModelIntrospector();
    }

    @Bean
    DataLoadChatSystemPromptProvider chatSystemPromptProvider() {
        return new DefaultDataLoadChatSystemPromptProvider();
    }

    @Bean
    AiAssistantSystemPromptProvider aiAssistantSystemPromptProvider() {
        return new DefaultAiAssistantSystemPromptProvider();
    }

    @Bean
    StubChatModel stubChatModel() {
        return new StubChatModel();
    }

    @Bean
    DefaultChatClientBuilder chatClientBuilder(StubChatModel stubChatModel) {
        return new DefaultChatClientBuilder(stubChatModel, ObservationRegistry.NOOP, null, null,
                ToolCallingAdvisor.builder());
    }

    @Bean
    AvailableEntityFilter availableEntityFilter() {
        return new DefaultAvailableEntityFilter();
    }

    @Bean
    AiToolDescriptorProvider aiToolDescriptorProvider() {
        return new AiToolDescriptorProviderImpl();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean
    UserRepository userRepository() {
        return new InMemoryUserRepository();
    }
}
