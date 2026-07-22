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

package io.jmix.autoconfigure.aitoolsflowuidata;

import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowuidata.AiToolsFlowuiDataConfiguration;
import io.jmix.aitoolsflowuidata.service.impl.AiChatDataService;
import io.jmix.aitoolsflowuidata.service.impl.AiConversationDataService;
import io.jmix.aitoolsflowuidata.service.impl.AiChatMessageDataService;
import io.jmix.aitoolsflowuidata.service.prompt.AiChatSystemPromptProvider;
import io.jmix.aitoolsflowuidata.service.prompt.impl.DefaultAiChatSystemPromptProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Provides the persistent implementations of the AI chat UI services. This starter is a pure
 * implementations provider: it does not bootstrap the FlowUI layer on its own - the application is
 * expected to add {@code jmix-aitools-flowui-starter} (chat UI) and {@code jmix-aitools-starter}
 * (core AI Tools beans) itself.
 * <p>
 * The persistent implementations depend on the core AI Tools beans, which are contributed by
 * {@code jmix-aitools-starter}. <strong>That starter is a required prerequisite of this one</strong>
 * - add it to the application alongside {@code jmix-aitools-flowui-data-starter}.
 * <p>
 * The whole configuration is gated on it being on the classpath (via {@code AiToolsAutoConfiguration},
 * referenced by name because this module does not depend on the core starter): when the core starter
 * is absent nothing is registered and the context still starts cleanly instead of failing with an
 * opaque missing-bean error, but persistent AI chat will not work until it is added.
 */
@AutoConfiguration(
        afterName = "io.jmix.autoconfigure.aitools.AiToolsAutoConfiguration",
        beforeName = "io.jmix.autoconfigure.aitoolsflowui.AiToolsFlowuiAutoConfiguration")
@ConditionalOnBooleanProperty(value = "jmix.aitools.ui.data.enabled", matchIfMissing = true)
@ConditionalOnClass(name = "io.jmix.autoconfigure.aitools.AiToolsAutoConfiguration")
@Import(AiToolsFlowuiDataConfiguration.class)
public class AiToolsFlowuiDataAutoConfiguration {

    @Bean("aitls_AiConversationDataService")
    @ConditionalOnMissingBean(AiConversationService.class)
    public AiConversationService userAiConversationService() {
        return new AiConversationDataService();
    }

    @Bean("aitls_AiChatMessageDataService")
    @ConditionalOnMissingBean(AiChatMessageService.class)
    public AiChatMessageService userAiMessageService() {
        return new AiChatMessageDataService();
    }

    @Bean("aitls_AiChatDataService")
    @ConditionalOnMissingBean(AiChatService.class)
    public AiChatService userAiChatService() {
        return new AiChatDataService();
    }

    @Bean("aitls_DefaultAiChatSystemPromptProvider")
    @ConditionalOnMissingBean(AiChatSystemPromptProvider.class)
    public AiChatSystemPromptProvider aiChatSystemPromptProvider() {
        return new DefaultAiChatSystemPromptProvider();
    }
}
