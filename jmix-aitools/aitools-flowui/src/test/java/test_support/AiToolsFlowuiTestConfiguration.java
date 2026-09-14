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
import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.CoreSecurityTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Boots the add-on's UI layer with in-memory chat services, so that views can be navigated to without a
 * database or a configured chat model.
 */
@Configuration
@Import({CoreConfiguration.class, FlowuiConfiguration.class, AiToolsConfiguration.class,
        AiToolsFlowuiConfiguration.class, CommonCoreTestConfiguration.class, CoreSecurityTestConfiguration.class,
        FlowuiServletTestBeans.class})
@JmixModule
public class AiToolsFlowuiTestConfiguration {

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

    @Bean
    TestAiConversationService conversationService() {
        return new TestAiConversationService();
    }

    @Bean
    TestAiChatMessageService chatMessageService() {
        return new TestAiChatMessageService();
    }

    @Bean
    TestAiChatService chatService() {
        return new TestAiChatService();
    }
}
