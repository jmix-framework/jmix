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

package io.jmix.texttodata.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.texttodata.generation.GeneratedJpqlParameter;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Collections;
import java.util.List;

public class SpringAiPromptExecutor {

    protected ChatClient chatClient;
    protected ObjectMapper objectMapper;

    protected String systemPrompt;

    public SpringAiPromptExecutor(ChatClient.Builder chatClientBuilder,
                                  ObjectMapper objectMapper,
                                  String systemPrompt) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;

        this.systemPrompt = systemPrompt;
    }

    public GeneratedJpqlResult executePrompt(String userPrompt) {
        String content = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("LLM returned an empty response");
        }

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
                payload.getWarnings() == null ? Collections.emptyList() : payload.getWarnings()
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
