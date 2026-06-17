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

import io.jmix.aitoolsflowui.service.UserAiChatService;
import io.jmix.aitoolsflowui.service.UserAiConversationService;
import io.jmix.aitoolsflowui.service.UserAiMessageService;
import io.jmix.aitoolsflowuidata.AiToolsFlowuiDataConfiguration;
import io.jmix.aitoolsflowuidata.service.impl.UserAiChatDataService;
import io.jmix.aitoolsflowuidata.service.impl.UserAiConversationDataService;
import io.jmix.aitoolsflowuidata.service.impl.UserAiMessageDataService;
import io.jmix.autoconfigure.aitoolsflowui.AiToolsFlowuiAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Activates the AI Tools FlowUI data module, providing persistent implementations of the chat UI
 * services. Each implementation is registered with {@link ConditionalOnMissingBean} so an
 * application can override any of them with its own bean. Configured before
 * {@link AiToolsFlowuiAutoConfiguration} so these implementations take precedence over its
 * no-op fallbacks. Can be disabled with the {@code jmix.aitools.ui.data.enabled=false} property.
 */
@AutoConfiguration(before = AiToolsFlowuiAutoConfiguration.class)
@ConditionalOnBooleanProperty(value = "jmix.aitools.ui.data.enabled", matchIfMissing = true)
@Import(AiToolsFlowuiDataConfiguration.class)
public class AiToolsFlowuiDataAutoConfiguration {

    @Bean("aitls_UserAiConversationDataService")
    @ConditionalOnMissingBean(UserAiConversationService.class)
    public UserAiConversationService userAiConversationService() {
        return new UserAiConversationDataService();
    }

    @Bean("aitls_UserAiMessageDataService")
    @ConditionalOnMissingBean(UserAiMessageService.class)
    public UserAiMessageService userAiMessageService() {
        return new UserAiMessageDataService();
    }

    @Bean("aitls_UserAiChatDataService")
    @ConditionalOnMissingBean(UserAiChatService.class)
    public UserAiChatService userAiChatService() {
        return new UserAiChatDataService();
    }
}
