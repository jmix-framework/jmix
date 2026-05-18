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
import io.jmix.aitools.dataload.executor.SpringAiPromptExecutor;
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.prompt.JpqlRepairerPromptProvider;
import io.jmix.aitools.dataload.prompt.DataLoadSystemPromptProvider;
import io.jmix.aitools.dataload.repair.JpqlRepairRequest;
import io.jmix.aitools.dataload.repair.JpqlRepairer;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashSet;
import java.util.Set;

public class SpringAiJpqlRepairer implements JpqlRepairer, InitializingBean {

    @Autowired
    protected JpqlRepairerPromptProvider jpqlRepairerPromptProvider;
    @Autowired
    protected DataLoadSystemPromptProvider dataLoadSystemPromptProvider;
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
        String repairPrompt = jpqlRepairerPromptProvider.getContent();
        String formattedPrompt = repairPrompt.formatted(
                request.getAttempt(),
                request.getExecutionRequest().getUserText(),
                toJson(request.getGeneratedJpqlResult()),
                formatValidationIssues(request.getValidationResult().getIssues()),
                formatRepairGuidance(request.getValidationResult().getIssues()));

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

    protected String formatRepairGuidance(java.util.List<JpqlValidationIssue> issues) {
        Set<String> guidance = new LinkedHashSet<>();
        guidance.add("Keep the same JSON contract as the previous result.");

        // TODO: pinyazhin, make extendable?
        for (JpqlValidationIssue issue : issues) {
            switch (issue.getCode()) {
                case "jpql.sqlPagination" ->
                        guidance.add("Remove LIMIT and OFFSET from JPQL and move them into maxResults and firstResult when the intent requires pagination.");
                case "jpql.sqlDateFunction" ->
                        guidance.add("Remove SQL-specific date arithmetic and vendor functions. Prefer supported Jmix date macros or relative date time constants, and use named parameters only when the date range cannot be expressed through supported constructs.");
                case "jpql.unsupportedMacro" ->
                        guidance.add("Use only supported Jmix date macros: @between, @today, @dateEquals, @dateBefore, @dateAfter.");
                case "jpql.currentFunctionParentheses" ->
                        guidance.add("Use CURRENT_DATE, CURRENT_TIME, and CURRENT_TIMESTAMP without parentheses.");
                case "jpql.syntax.invalid" ->
                        guidance.add("Rewrite the JPQL into valid JPQL syntax only. Do not keep SQL keywords or malformed JPQL fragments.");
                case "propertyPath.invalid" ->
                        guidance.add("Replace invalid property paths with valid paths from the provided schema only.");
                case "rootEntity.unknown", "usedEntity.unknown" ->
                        guidance.add("Use only entity names that are present in the provided schema.");
                case "parameter.missingInDto" ->
                        guidance.add("Ensure every named JPQL parameter is declared in the parameters array.");
                case "parameter.unusedInJpql" ->
                        guidance.add("Remove parameters that are not used in the JPQL text.");
                case "jpql.notSelect", "jpql.writeOperation" ->
                        guidance.add("Return a read-only select JPQL query only.");
                default -> {
                }
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

    protected SpringAiPromptExecutor createPromptExecutor() {
        return new SpringAiPromptExecutor(chatModelProvider, objectMapper, dataLoadSystemPromptProvider.getContent());
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
