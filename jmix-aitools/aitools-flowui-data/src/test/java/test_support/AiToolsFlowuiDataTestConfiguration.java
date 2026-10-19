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
import io.jmix.aitools.service.prompt.AiAssistantSystemPromptProvider;
import io.jmix.aitools.service.prompt.impl.DefaultAiAssistantSystemPromptProvider;
import io.jmix.aitools.tool.AiToolDescriptorProvider;
import io.jmix.aitools.tool.impl.AiToolDescriptorProviderImpl;
import io.jmix.aitoolsflowui.AiToolsFlowuiConfiguration;
import io.jmix.aitoolsflowui.icon.AiIconProvider;
import io.jmix.aitoolsflowui.icon.impl.DefaultAiIconProvider;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.aitoolsflowui.service.AiConversationTitleGenerator;
import io.jmix.aitoolsflowui.service.impl.AiConversationTitleEmptyGenerator;
import io.jmix.aitoolsflowuidata.AiToolsFlowuiDataConfiguration;
import io.jmix.aitoolsflowuidata.service.impl.AiChatDataService;
import io.jmix.aitoolsflowuidata.service.impl.AiChatMessageDataService;
import io.jmix.aitoolsflowuidata.service.impl.AiConversationDataService;
import io.jmix.aitoolsflowuidata.service.prompt.AiChatSystemPromptProvider;
import io.jmix.aitoolsflowuidata.service.prompt.impl.DefaultAiChatSystemPromptProvider;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.CoreSecurityTestConfiguration;
import io.jmix.testsupport.config.HsqlEmbeddedDataSourceTestConfiguration;
import io.jmix.testsupport.config.JpaMainStoreTestConfiguration;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Boots the persistent AI chat services on an embedded HSQL database with a {@link TestChatModel} in place of a
 * real model.
 */
@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class,
        FlowuiConfiguration.class, AiToolsConfiguration.class, AiToolsFlowuiConfiguration.class,
        AiToolsFlowuiDataConfiguration.class, CommonCoreTestConfiguration.class,
        CoreSecurityTestConfiguration.class, HsqlEmbeddedDataSourceTestConfiguration.class,
        JpaMainStoreTestConfiguration.class, FlowuiServletTestBeans.class})
@JmixModule(dependsOn = {FlowuiConfiguration.class, AiToolsFlowuiDataConfiguration.class,
        EclipselinkConfiguration.class})
public class AiToolsFlowuiDataTestConfiguration {

    @Bean
    TestChatModel testChatModel() {
        return new TestChatModel();
    }

    @Bean
    DefaultChatClientBuilder chatClientBuilder(TestChatModel chatModel) {
        return new DefaultChatClientBuilder(chatModel, ObservationRegistry.NOOP, null, null,
                ToolCallingAdvisor.builder());
    }

    @Bean
    AiIconProvider aiIconProvider() {
        return new DefaultAiIconProvider();
    }

    @Bean
    AiAssistantSystemPromptProvider aiAssistantSystemPromptProvider() {
        return new DefaultAiAssistantSystemPromptProvider();
    }

    @Bean
    AiToolDescriptorProvider aiToolDescriptorProvider() {
        return new AiToolDescriptorProviderImpl();
    }

    @Bean("aitls_AiConversationDataService")
    AiConversationService conversationService() {
        return new AiConversationDataService();
    }

    @Bean("aitls_AiChatMessageDataService")
    AiChatMessageService chatMessageService() {
        return new AiChatMessageDataService();
    }

    @Bean("aitls_AiChatDataService")
    AiChatService chatService() {
        return new AiChatDataService();
    }

    @Bean("aitls_DefaultAiChatSystemPromptProvider")
    AiChatSystemPromptProvider aiChatSystemPromptProvider() {
        return new DefaultAiChatSystemPromptProvider();
    }

    @Bean
    AiConversationTitleGenerator aiConversationTitleGenerator() {
        return new AiConversationTitleEmptyGenerator();
    }
}
