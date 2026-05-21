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

package io.jmix.aitools.dataload.repair.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.prompt.JpqlRepairerPromptProvider;
import io.jmix.aitools.dataload.repair.JpqlRepairRequest;
import io.jmix.aitools.dataload.repair.JpqlRepairer;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class DefaultJpqlRepairer implements JpqlRepairer, InitializingBean {

    @Autowired
    protected JpqlRepairerPromptProvider jpqlRepairerPromptProvider;
    @Autowired
    protected ChatClientFactory chatClientFactory;

    protected ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() {
        objectMapper = createObjectMapper();
    }

    @Override
    public GeneratedJpqlResult repair(JpqlRepairRequest request) {
        return executePrompt(request);
    }

    protected String formatValidationIssues(java.util.List<JpqlValidationIssue> issues) {
        StringBuilder builder = new StringBuilder();
        for (JpqlValidationIssue issue : issues) {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append("- ")
                    .append(issue.getCode())
                    .append(": ")
                    .append(issue.getMessage());
        }
        return builder.toString();
    }

    protected String formatRepairGuidance(java.util.List<JpqlValidationIssue> issues) {
        Set<String> guidance = new LinkedHashSet<>();
        guidance.add("Keep the same JSON contract as the previous result.");

        for (JpqlValidationIssue issue : issues) {
            String guidanceStr = issue.getGuidance();
            if (guidanceStr != null) {
                guidance.add(guidanceStr);
            }
        }

        StringBuilder builder = new StringBuilder();
        for (String line : guidance) {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append("- ").append(line);
        }
        return builder.toString();
    }

    protected GeneratedJpqlResult executePrompt(JpqlRepairRequest request) {
        String repairPrompt = jpqlRepairerPromptProvider.getContent();
        String userPrompt = repairPrompt.formatted(
                request.getAttempt(),
                request.getExecutionRequest().getUserText(),
                toJson(request.getGeneratedJpqlResult()),
                formatValidationIssues(request.getValidationResult().getIssues()),
                formatRepairGuidance(request.getValidationResult().getIssues()));

        String content = chatClientFactory.createChatClient(builder ->
                        builder.defaultAdvisors(SimpleLoggerAdvisor.builder().build()))
                .prompt(userPrompt)
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("LLM returned an empty response");
        }

        GeneratedJpqlPayload payload;
        try {
            payload = objectMapper.readValue(content, GeneratedJpqlPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse LLM response as JSON: " + content, e);
        }

        return mapToGeneratedJpqlResult(payload);
    }

    protected GeneratedJpqlResult mapToGeneratedJpqlResult(GeneratedJpqlPayload payload) {
        List<GeneratedJpqlParameter> parameters = payload.getParameters().stream()
                .map(parameter -> new GeneratedJpqlParameter(parameter.getName(), parameter.getType(), parameter.getValue()))
                .toList();

        return new GeneratedJpqlResult(payload.getJpql(), parameters, payload.getExplanation(),
                payload.getWarnings() == null ? Collections.emptyList() : payload.getWarnings(),
                payload.getMaxResults(), payload.getFirstResult()
        );
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    protected String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize object to JSON", e);
        }
    }
}
