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

package io.jmix.aitools.dataload;

import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.dataload.execution.*;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.dataload.prompt.DataLoadChatSystemPromptProvider;
import io.jmix.aitools.dataload.tool.DataLoadAiTool;
import io.jmix.aitools.memory.ChatMemoryFactory;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.aitools.tool.ResolvedAiTool;
import io.jmix.aitools.memory.JmixChatMemoryRepository;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

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
    protected ChatMemoryFactory chatMemoryFactory;
    @Autowired
    protected DataLoadChatSystemPromptProvider systemPromptProvider;
    @Autowired
    protected AiToolRegistry aiToolRegistry;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected EntityDataLoadGenerationService entityDataLoadGenerationService;
    @Autowired
    protected JpqlExecutionService jpqlExecutionService;

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
    public Flux<String> stream(String message) {
        return stream(message, JmixChatMemoryRepository.NO_OP_CONVERSATION_ID);
    }

    @Override
    public Flux<String> stream(String message, String conversationId) {
        Preconditions.checkNotEmptyString(message);
        Preconditions.checkNotEmptyString(conversationId);

        checkChatClient();

        return executePromptStream(message, conversationId);
    }

    @Override
    public String send(String message, String conversationId) {
        Preconditions.checkNotEmptyString(message);
        Preconditions.checkNotEmptyString(conversationId);

        checkChatClient();

        return executePrompt(message, conversationId);
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

    @Override
    public EntityDataLoadResult loadData(String userText, String conversationId) {
        Preconditions.checkNotEmptyString(userText);
        Preconditions.checkNotEmptyString(conversationId);

        EntityDataLoadQuery queryDraft = entityDataLoadGenerationService.generate(userText, conversationId);

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

    protected String executePrompt(String message, String conversationId) {
        return buildChatClientPrompt(message, conversationId)
                .call()
                .content();
    }

    protected Flux<String> executePromptStream(String message, String conversationId) {
        return buildChatClientPrompt(message, conversationId)
                .stream()
                .content();
    }

    protected ChatClient.ChatClientRequestSpec buildChatClientPrompt(String message, String conversationId) {
        return chatClient.prompt()
                .system(system -> system
                        .text(systemPromptProvider.getResource())
                        .param("responseLanguage", resolveResponseLanguage()))
                .user(user -> user.text(message))
                .toolCallbacks(aiToolRegistry.findByMarker(DataLoadAiTool.class).stream()
                        .map(ResolvedAiTool::getCallback)
                        .toList())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId));
    }

    protected void buildChatClient() {
        chatClient = chatClientFactory.createChatClient(builder ->
                builder.defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        MessageChatMemoryAdvisor.builder(buildChatMemory()).build()
                ));
    }

    protected ChatMemory buildChatMemory() {
        return chatMemoryFactory.build();
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
