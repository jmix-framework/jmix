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

package io.jmix.aitools.service.impl;

import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.service.AiChatService;
import io.jmix.aitools.service.prompt.AiChatSystemPromptProvider;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component("aitols_AiChatServiceImpl")
public class AiChatServiceImpl implements AiChatService, InitializingBean {

    @Autowired
    protected ChatClientFactory chatClientFactory;
    @Autowired
    protected AiToolRegistry aiToolRegistry;
    @Autowired
    protected AiChatSystemPromptProvider systemPromptProvider;
    @Autowired
    protected CurrentAuthentication currentAuthentication;

    protected ChatClient chatClient;

    @Override
    public void afterPropertiesSet() {
        chatClient = chatClientFactory.createChatClientWithoutMemoryAdvisor();
    }

    @Override
    public String send(String message) {
        Preconditions.checkNotEmptyString(message);
        checkChatClient();
        return buildPrompt(message).call().content();
    }

    @Override
    public Flux<String> stream(String message) {
        Preconditions.checkNotEmptyString(message);
        checkChatClient();
        return buildPrompt(message).stream().content();
    }

    protected ChatClient.ChatClientRequestSpec buildPrompt(String message) {
        return chatClient.prompt()
                .system(s -> s
                        .text(systemPromptProvider.getResource())
                        .param("responseLanguage", resolveResponseLanguage())
                        .param("additionalInstructions", ""))
                .user(user -> user.text(message))
                .tools(t -> t.callbacks(aiToolRegistry.getAllCallbacks()));
    }

    protected String resolveResponseLanguage() {
        return currentAuthentication.getLocale().getLanguage();
    }

    protected void checkChatClient() {
        if (chatClient == null) {
            throw new IllegalStateException(ChatClient.class.getSimpleName() + " is not configured in application");
        }
    }
}
