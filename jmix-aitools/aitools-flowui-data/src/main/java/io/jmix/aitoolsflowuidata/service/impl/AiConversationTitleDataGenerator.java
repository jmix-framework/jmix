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

package io.jmix.aitoolsflowuidata.service.impl;

import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.ResponseLanguageProvider;
import io.jmix.aitoolsflowui.service.AiConversationTitleGenerator;
import io.jmix.aitoolsflowui.view.chat.support.ConversationTitleSupport;
import io.jmix.aitoolsflowuidata.service.prompt.AiConversationTitleSystemPromptProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Default {@link AiConversationTitleGenerator}: asks the configured Spring AI model in a single call without
 * tools and history. Returns empty when no model is configured or the call fails.
 */
public class AiConversationTitleDataGenerator implements AiConversationTitleGenerator {

    private static final Logger log = LoggerFactory.getLogger(AiConversationTitleDataGenerator.class);

    @Autowired
    protected ChatClientFactory chatClientFactory;
    @Autowired
    protected ResponseLanguageProvider responseLanguageProvider;
    @Autowired
    protected ConversationTitleSupport titleSupport;
    @Autowired
    protected AiConversationTitleSystemPromptProvider promptProvider;

    @Override
    public Optional<String> generateTitle(String firstUserMessage) {
        if (!chatClientFactory.isConfigured() || firstUserMessage.isBlank()) {
            return Optional.empty();
        }
        try {
            ChatClient client = chatClientFactory.createChatClient(builder ->
                    builder.defaultAdvisors(SimpleLoggerAdvisor.builder().build()));
            String raw = client.prompt()
                    .system(s -> s.text(promptProvider.getResource())
                            .param("responseLanguage", responseLanguageProvider.getResponseLanguage()))
                    .user(firstUserMessage)
                    .call()
                    .content();
            return Optional.ofNullable(titleSupport.sanitizeGeneratedTitle(raw));
        } catch (RuntimeException e) {
            log.warn("Failed to generate AI conversation title", e);
            return Optional.empty();
        }
    }
}
