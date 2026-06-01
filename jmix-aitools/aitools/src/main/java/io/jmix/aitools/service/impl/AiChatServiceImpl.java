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
import io.jmix.aitools.memory.JmixChatMemoryRepository;
import io.jmix.aitools.service.AiChatService;
import io.jmix.aitools.service.prompt.AiChatSystemPromptProvider;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
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
        buildChatClient();
    }

    @Override
    public String send(String message) {
        return send(message, JmixChatMemoryRepository.NO_OP_CONVERSATION_ID);
    }

    @Override
    public String send(String message, String conversationId) {
        Preconditions.checkNotEmptyString(message);
        Preconditions.checkNotEmptyString(conversationId);

        checkChatClient();

        ChatClient.CallResponseSpec callResponseSpec = buildPrompt(message, conversationId).call();

        return callResponseSpec.content();
    }

    @Override
    public Flux<String> stream(String message) {
        return stream(message, JmixChatMemoryRepository.NO_OP_CONVERSATION_ID);
    }

    @Override
    public Flux<String> stream(String message, String conversationId) {
        Preconditions.checkNotEmptyString(message);
        Preconditions.checkNotEmptyString(conversationId);

        checkChatClient();

        ChatClient.StreamResponseSpec streamResponseSpec = buildPrompt(message, conversationId).stream();

        return streamResponseSpec.content();
    }

    protected ChatClient.ChatClientRequestSpec buildPrompt(String message, String conversationId) {
        return chatClient.prompt()
                .system(s -> s
                        .text(systemPromptProvider.getResource())
                        .param("responseLanguage", resolveResponseLanguage())
                        .param("additionalInstructions", ""))
                .user(user -> user.text(message))
                .toolCallbacks(aiToolRegistry.getAllCallbacks())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId));
    }

    protected String resolveResponseLanguage() {
        return currentAuthentication.getLocale().getLanguage();
    }

    protected void buildChatClient() {
        chatClient = chatClientFactory.createChatClientWithDefaultAdvisors();
    }

    protected void checkChatClient() {
        if (chatClient == null) {
            throw new IllegalStateException(ChatClient.class.getSimpleName() + " is not configured in application");
        }
    }
}
