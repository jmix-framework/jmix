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

import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.ResponseLanguageProvider;
import io.jmix.aitools.dataload.AiDataLoadService;
import io.jmix.aitools.dataload.EntityDataLoadQuery;
import io.jmix.aitools.dataload.EntityDataLoadResult;
import io.jmix.aitools.dataload.execution.*;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.dataload.prompt.DataLoadChatSystemPromptProvider;
import io.jmix.aitools.dataload.tool.DataLoadAiTool;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.aitools.tool.ResolvedAiTool;
import io.jmix.core.common.util.Preconditions;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The default implementation of {@link AiDataLoadService}.
 * <p>
 * It uses {@link DataLoadAiTool} tools (entity discovery, JPQL execution, etc.) to get a valid JPQL query from LLM.
 * This JPQL is validated and can be repaired if it is required.
 *
 * @see EntityDataLoadGenerationService
 */
@Component("aitols_AiDataLoadServiceImpl")
public class AiDataLoadServiceImpl implements AiDataLoadService, InitializingBean {

    @Autowired
    protected ChatClientFactory chatClientFactory;
    @Autowired
    protected DataLoadChatSystemPromptProvider systemPromptProvider;
    @Autowired
    protected AiToolRegistry aiToolRegistry;
    @Autowired
    protected ResponseLanguageProvider responseLanguageProvider;
    @Autowired
    protected EntityDataLoadGenerationService entityDataLoadGenerationService;
    @Autowired
    protected JpqlExecutionService jpqlExecutionService;

    @Nullable
    protected ChatClient chatClient;

    @Override
    public void afterPropertiesSet() {
        buildChatClient();
    }

    @Nullable
    @Override
    public String send(String message) {
        Preconditions.checkNotEmptyString(message);

        return buildChatClientPrompt(message)
                .call()
                .content();
    }

    @Override
    public Flux<String> stream(String message) {
        Preconditions.checkNotEmptyString(message);

        return buildChatClientPrompt(message)
                .stream()
                .content();
    }

    @Override
    public EntityDataLoadResult loadData(String userText) {
        Preconditions.checkNotEmptyString(userText);

        EntityDataLoadQuery queryDraft = entityDataLoadGenerationService.generate(userText);

        JpqlExecutionResult executionResult = jpqlExecutionService.execute(
                new JpqlExecutionRequest(
                        userText,
                        queryDraft.getJpql(),
                        toExecutionParameters(queryDraft.getParameters()),
                        queryDraft.getResultProperties(),
                        queryDraft.getMaxResults(),
                        queryDraft.getFirstResult()
                )
        );

        return new EntityDataLoadResult(
                userText,
                queryDraft,
                executionResult.getValidationResult(),
                executionResult.getRows(),
                executionResult.isHasMore(),
                executionResult.isExecuted(),
                executionResult.getExecutionError()
        );
    }

    protected ChatClient.ChatClientRequestSpec buildChatClientPrompt(String message) {
        checkChatClient();

        return Objects.requireNonNull(chatClient).prompt()
                .system(system -> system
                        .text(systemPromptProvider.getResource())
                        .param("responseLanguage", resolveResponseLanguage()))
                .user(user -> user.text(message))
                .tools(aiToolRegistry.findByMarker(DataLoadAiTool.class).stream()
                        .map(ResolvedAiTool::getCallback)
                        .toList());
    }

    protected void buildChatClient() {
        chatClient = chatClientFactory.createChatClientWithDefaultAdvisors().orElse(null);
    }

    protected String resolveResponseLanguage() {
        return responseLanguageProvider.getResponseLanguage();
    }

    protected void checkChatClient() {
        if (chatClient == null) {
            throw new IllegalStateException(ChatClient.class.getSimpleName() + " is not configured in application");
        }
    }

    protected List<JpqlExecutionParameter> toExecutionParameters(List<GeneratedJpqlParameter> generatedParameters) {
        if (generatedParameters.isEmpty()) {
            return List.of();
        }

        List<JpqlExecutionParameter> executionParameters = new ArrayList<>(generatedParameters.size());
        for (GeneratedJpqlParameter parameter : generatedParameters) {
            executionParameters.add(new JpqlExecutionParameter(
                    parameter.getName(),
                    parameter.getType(),
                    parameter.getValue()
            ));
        }
        return List.copyOf(executionParameters);
    }
}
