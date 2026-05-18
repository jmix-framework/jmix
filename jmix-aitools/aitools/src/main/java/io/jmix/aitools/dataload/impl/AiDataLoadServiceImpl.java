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

package io.jmix.aitools.dataload.impl;

import io.jmix.aitools.dataload.AiDataLoadService;
import io.jmix.aitools.dataload.prompt.DataLoadSystemPromptProvider;
import io.jmix.aitools.dataload.tool.DataLoadToolCallbackProvider;
import io.jmix.aitools.memory.ChatMemoryProvider;
import io.jmix.aitools.memory.JmixChatMemoryRepository;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("aitols_AiDataLoadServiceImpl")
public class AiDataLoadServiceImpl implements AiDataLoadService, InitializingBean {

    @Autowired
    protected ObjectProvider<ChatClient.Builder> chatClientBuilder;
    @Autowired
    protected ChatMemoryProvider chatMemoryProvider;
    @Autowired
    protected DataLoadSystemPromptProvider systemPromptProvider;
    @Autowired
    protected DataLoadToolCallbackProvider dataLoadToolCallbackProvider;
    @Autowired
    protected CurrentAuthentication currentAuthentication;

    protected ChatClient chatClient;

    @Override
    public void afterPropertiesSet() {
        chatClientBuilder.ifAvailable(this::buildChatClient);
    }

    @Override
    public String sendMessage(String message) {
        return sendMessage(message, JmixChatMemoryRepository.NO_OP_CONVERSATION_ID);
    }

    @Override
    public String sendMessage(String message, String conversationId) {
        Preconditions.checkNotEmptyString(message);
        Preconditions.checkNotEmptyString(conversationId);

        checkChatClient();

        return executePrompt(message, conversationId);
    }

    // TODO: pinyazhin, support attachments
    protected String executePrompt(String message, String conversationId) {
        return chatClient.prompt()
                .system(system -> system
                        .text(systemPromptProvider.getResource())
                        .param("responseLanguage", resolveResponseLanguage()))
                .user(user -> user.text(message))
                .toolCallbacks(dataLoadToolCallbackProvider.getToolCallbacks())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    protected void buildChatClient(ChatClient.Builder builder) {
        chatClient = builder
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        MessageChatMemoryAdvisor.builder(buildChatMemory()).build()
                )
                .build();
    }

    protected ChatMemory buildChatMemory() {
        return chatMemoryProvider.getChatMemory();
    }

    protected boolean isChatClientAvailable() {
        return chatClient != null;
    }

    protected String resolveResponseLanguage() {
        return currentAuthentication.getLocale().getLanguage();
    }

    protected void checkChatClient() {
        if (!isChatClientAvailable()) {
            throw new IllegalStateException(ChatClient.class.getSimpleName() + " is not configured in application");
        }
    }
}
