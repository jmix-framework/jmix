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

package io.jmix.autoconfigure.aitoolsflowui;

import io.jmix.aitoolsflowui.AiToolsFlowuiConfiguration;
import io.jmix.aitoolsflowui.icon.AiIconProvider;
import io.jmix.aitoolsflowui.icon.impl.DefaultAiIconProvider;
import io.jmix.aitoolsflowui.service.UserAiChatService;
import io.jmix.aitoolsflowui.service.UserAiConversationService;
import io.jmix.aitoolsflowui.service.UserAiMessageService;
import io.jmix.aitoolsflowui.service.impl.UserAiChatEmptyService;
import io.jmix.aitoolsflowui.service.impl.UserAiConversationEmptyService;
import io.jmix.aitoolsflowui.service.impl.UserAiMessageEmptyService;
import io.jmix.flowui.FlowuiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AiToolsFlowuiConfiguration.class, FlowuiConfiguration.class})
public class AiToolsFlowuiAutoConfiguration {

    @Bean("aitls_DefaultAiIconProvider")
    @ConditionalOnMissingBean(AiIconProvider.class)
    public AiIconProvider aiIconProvider() {
        return new DefaultAiIconProvider();
    }

    @Bean("aitls_UserAiChatEmptyService")
    @ConditionalOnMissingBean(UserAiChatService.class)
    public UserAiChatService userAiChatService() {
        return new UserAiChatEmptyService();
    }

    @Bean("aitls_UserAiConversationEmptyService")
    @ConditionalOnMissingBean(UserAiConversationService.class)
    public UserAiConversationService userAiConversationService() {
        return new UserAiConversationEmptyService();
    }

    @Bean("aitls_UserAiMessageEmptyService")
    @ConditionalOnMissingBean(UserAiMessageService.class)
    public UserAiMessageService userAiMessageService() {
        return new UserAiMessageEmptyService();
    }
}
