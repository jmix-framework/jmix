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

package io.jmix.texttodata.repair.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.texttodata.executor.SpringAiPromptExecutor;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.prompt.JpqlRepairerPromptProvider;
import io.jmix.texttodata.prompt.SystemPromptProvider;
import io.jmix.texttodata.repair.JpqlRepairRequest;
import io.jmix.texttodata.repair.JpqlRepairer;
import io.jmix.texttodata.validation.JpqlValidationIssue;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringAiJpqlRepairer implements JpqlRepairer, InitializingBean {

    @Autowired
    protected JpqlRepairerPromptProvider jpqlRepairerPromptProvider;
    @Autowired
    protected SystemPromptProvider systemPromptProvider;
    @Autowired
    protected ObjectProvider<ChatModel> chatModelProvider;

    protected SpringAiPromptExecutor promptExecutor;
    protected ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() {
        objectMapper = createObjectMapper();
        promptExecutor = createPromptExecutor();
    }

    @Override
    public GeneratedJpqlResult repair(JpqlRepairRequest request) {
        String repairPrompt = jpqlRepairerPromptProvider.get();
        String formattedPrompt = repairPrompt.formatted(
                request.getGenerationRequest().getUserText(),
                request.getGenerationRequest().getPromptContext(),
                toJson(request.getGeneratedJpqlResult()),
                formatValidationIssues(request.getValidationResult().getIssues()));

        return promptExecutor.executePrompt(formattedPrompt);
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

    protected SpringAiPromptExecutor createPromptExecutor() {
        return new SpringAiPromptExecutor(chatModelProvider, objectMapper, systemPromptProvider.get());
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
