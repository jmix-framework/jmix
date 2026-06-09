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
import io.jmix.aitools.ResponseLanguageProvider;
import io.jmix.aitools.dataload.EntityDataLoadQuery;
import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.dataload.generation.EntityDataLoadQueryPayload;
import io.jmix.aitools.dataload.prompt.EntityDataLoadPromptProvider;
import io.jmix.aitools.dataload.repair.impl.GeneratedJpqlParameterPayload;
import io.jmix.aitools.dataload.tool.EntityDataLoadAiTool;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.aitools.tool.ResolvedAiTool;
import io.jmix.core.common.util.Preconditions;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Objects;

/**
 * Default {@link EntityDataLoadGenerationService} implementation based on an LLM chat client.
 */
@Component("aitols_EntityDataLoadGenerationService")
public class EntityDataLoadGenerationServiceImpl implements EntityDataLoadGenerationService, InitializingBean {

    @Autowired
    protected ChatClientFactory chatClientFactory;
    @Autowired
    protected EntityDataLoadPromptProvider entityDataLoadPromptProvider;
    @Autowired
    protected AiToolRegistry aiToolRegistry;
    @Autowired
    protected ResponseLanguageProvider responseLanguageProvider;

    @Nullable
    protected ChatClient chatClient;
    protected ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() {
        objectMapper = createObjectMapper();
        buildChatClient();
    }

    @Override
    public EntityDataLoadQuery generate(String userText) {
        Preconditions.checkNotEmptyString(userText);

        String content = buildChatClientPrompt(userText)
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

    protected ChatClient.ChatClientRequestSpec buildChatClientPrompt(String userText) {
        checkChatClient();

        return Objects.requireNonNull(chatClient)
                .prompt()
                .system(system -> system
                        .text(entityDataLoadPromptProvider.getResource())
                        .param("responseLanguage", resolveResponseLanguage()))
                .user(user -> user.text(userText))
                .tools(aiToolRegistry.findByMarker(EntityDataLoadAiTool.class).stream()
                        .map(ResolvedAiTool::getCallback)
                        .toList());
    }

    protected void buildChatClient() {
        chatClient = chatClientFactory.createChatClientWithDefaultAdvisors().orElse(null);
    }

    protected void checkChatClient() {
        if (chatClient == null) {
            throw new IllegalStateException(ChatClient.class.getSimpleName() + " is not configured in application");
        }
    }

    protected String resolveResponseLanguage() {
        return responseLanguageProvider.getResponseLanguage();
    }

    protected EntityDataLoadQuery mapToQueryDraft(EntityDataLoadQueryPayload payload) {
        List<GeneratedJpqlParameter> parameters = payload.getParameters() == null
                ? Collections.emptyList()
                : payload.getParameters().stream()
                  .map(this::toGeneratedJpqlParameter)
                  .filter(Objects::nonNull)
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

    /**
     * Maps a parameter payload to a {@link GeneratedJpqlParameter}, or returns {@code null} to skip
     * it when the model provided no parameter name (such a parameter cannot be bound).
     */
    @Nullable
    protected GeneratedJpqlParameter toGeneratedJpqlParameter(GeneratedJpqlParameterPayload payload) {
        String name = payload.getName();
        if (name == null || name.isBlank()) {
            return null;
        }
        return new GeneratedJpqlParameter(name, Objects.requireNonNullElse(payload.getType(), ""), payload.getValue());
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}
