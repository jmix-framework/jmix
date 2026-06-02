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

package io.jmix.aitools.dataload.generation.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.dataload.EntityDataLoadQuery;
import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.dataload.generation.EntityDataLoadQueryPayload;
import io.jmix.aitools.dataload.prompt.EntityDataLoadPromptProvider;
import io.jmix.aitools.dataload.repair.impl.GeneratedJpqlParameterPayload;
import io.jmix.aitools.dataload.tool.EntityDataLoadAiTool;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.aitools.tool.ResolvedAiTool;
import io.jmix.aitools.memory.JmixChatMemoryRepository;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("aitols_EntityDataLoadGenerationService")
public class EntityDataLoadGenerationServiceImpl implements EntityDataLoadGenerationService, InitializingBean {

    @Autowired
    protected ChatClientFactory chatClientFactory;
    @Autowired
    protected EntityDataLoadPromptProvider entityDataLoadPromptProvider;
    @Autowired
    protected AiToolRegistry aiToolRegistry;
    @Autowired
    protected CurrentAuthentication currentAuthentication;

    protected ChatClient chatClient;
    protected ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() {
        objectMapper = createObjectMapper();
        buildChatClient();
    }

    @Override
    public EntityDataLoadQuery generate(String userText) {
        return generate(userText, JmixChatMemoryRepository.NO_OP_CONVERSATION_ID);
    }

    @Override
    public EntityDataLoadQuery generate(String userText, String conversationId) {
        Preconditions.checkNotEmptyString(userText);
        Preconditions.checkNotEmptyString(conversationId);

        checkChatClient();

        String content = buildChatClientPrompt(userText, conversationId)
                .call()
                .content();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("LLM returned an empty response");
        }

        EntityDataLoadQueryPayload payload;
        try {
            payload = objectMapper.readValue(content, EntityDataLoadQueryPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse LLM response as JSON: " + content, e);
        }

        return mapToQueryDraft(payload);
    }

    protected ChatClient.ChatClientRequestSpec buildChatClientPrompt(String userText, String conversationId) {
        return chatClient.prompt()
                .system(system -> system
                        .text(entityDataLoadPromptProvider.getResource())
                        .param("responseLanguage", resolveResponseLanguage()))
                .user(user -> user.text(userText))
                .tools(t -> t.callbacks(aiToolRegistry.findByMarker(EntityDataLoadAiTool.class).stream()
                        .map(ResolvedAiTool::getCallback)
                        .toList()))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId));
    }

    protected void buildChatClient() {
        chatClient = chatClientFactory.createChatClientWithDefaultAdvisors();
    }

    protected boolean isChatClientAvailable() {
        return chatClient != null;
    }

    protected void checkChatClient() {
        if (!isChatClientAvailable()) {
            throw new IllegalStateException(ChatClient.class.getSimpleName() + " is not configured in application");
        }
    }

    protected String resolveResponseLanguage() {
        return currentAuthentication.getLocale().getLanguage();
    }

    protected EntityDataLoadQuery mapToQueryDraft(EntityDataLoadQueryPayload payload) {
        List<GeneratedJpqlParameter> parameters = payload.getParameters() == null
                ? Collections.emptyList()
                : payload.getParameters().stream()
                .map(this::toGeneratedJpqlParameter)
                .toList();

        return new EntityDataLoadQuery(
                payload.getJpql(),
                parameters,
                payload.getResultProperties() == null ? Collections.emptyList() : payload.getResultProperties(),
                payload.getExplanation(),
                payload.getWarnings() == null ? Collections.emptyList() : payload.getWarnings(),
                payload.getMaxResults(),
                payload.getFirstResult()
        );
    }

    protected GeneratedJpqlParameter toGeneratedJpqlParameter(GeneratedJpqlParameterPayload payload) {
        return new GeneratedJpqlParameter(payload.getName(), payload.getType(), payload.getValue());
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}
