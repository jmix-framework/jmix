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

package io.jmix.texttodata.dataload.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.texttodata.dataload.generation.GeneratedJpqlParameter;
import io.jmix.texttodata.dataload.generation.GeneratedJpqlResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Collections;
import java.util.List;

public class SpringAiPromptExecutor {
    private static final Logger log = LoggerFactory.getLogger(SpringAiPromptExecutor.class);

    protected ChatClient chatClient;
    protected ObjectMapper objectMapper;

    protected String systemPrompt;

    public SpringAiPromptExecutor(ObjectProvider<ChatModel> chatModelProvider,
                                  ObjectMapper objectMapper,
                                  String systemPrompt) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            throw new IllegalStateException("No " + ChatModel.class.getSimpleName() + " available");
        }

        this.chatClient = ChatClient.create(chatModel);
        this.objectMapper = objectMapper;

        this.systemPrompt = systemPrompt;
    }

    public GeneratedJpqlResult executePrompt(String generatedPrompt) {
        log.debug("\n\nPrompt: {}", generatedPrompt);
        log.debug("\n\nSystem prompt: {}", systemPrompt);

        String content = chatClient.prompt()
                .system(systemPrompt)
                .user(generatedPrompt)
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("LLM returned an empty response");
        }

        log.debug("\n\nReturned content: {}", content);

        SpringAiGeneratedJpqlPayload payload;
        try {
            payload = objectMapper.readValue(stripMarkdownCodeFence(content), SpringAiGeneratedJpqlPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse LLM response as JSON: " + content, e);
        }

        return mapToGeneratedJpqlResult(payload);
    }

    protected GeneratedJpqlResult mapToGeneratedJpqlResult(SpringAiGeneratedJpqlPayload payload) {
        List<GeneratedJpqlParameter> parameters = payload.getParameters() == null
                ? Collections.emptyList()
                : payload.getParameters().stream()
                  .map(parameter -> new GeneratedJpqlParameter(
                          parameter.getName(),
                          parameter.getType(),
                          parameter.getValue()))
                  .toList();

        return new GeneratedJpqlResult(
                payload.getJpql(),
                payload.getRootEntityName(),
                parameters,
                payload.getUsedEntities() == null ? Collections.emptyList() : payload.getUsedEntities(),
                payload.getUsedPropertyPaths() == null ? Collections.emptyList() : payload.getUsedPropertyPaths(),
                payload.getExplanation(),
                payload.getWarnings() == null ? Collections.emptyList() : payload.getWarnings(),
                payload.getMaxResults(),
                payload.getFirstResult()
        );
    }

    // TODO: pinyazhin, do we need this?
    protected String stripMarkdownCodeFence(String content) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        int firstLineEnd = trimmed.indexOf('\n');
        if (firstLineEnd < 0) {
            return trimmed;
        }

        int closingFence = trimmed.lastIndexOf("```");
        if (closingFence <= firstLineEnd) {
            return trimmed;
        }

        return trimmed.substring(firstLineEnd + 1, closingFence).trim();
    }
}
