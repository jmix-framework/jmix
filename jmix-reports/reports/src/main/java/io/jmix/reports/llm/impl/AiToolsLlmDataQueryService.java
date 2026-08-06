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

package io.jmix.reports.llm.impl;

import io.jmix.aitools.dataload.EntityDataLoadQuery;
import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.execution.JpqlExecutionParameter;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.LlmQueryExecutionRequest;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements the Reports-side seam on top of the AI Tools data-load subsystem: generation produces the
 * query, and execution runs it through {@code DataManager}, which is what applies the current user's data
 * access constraints.
 * <p>
 * The only class in Reports that depends on the AI Tools add-on, and therefore the only one that must not
 * be loaded when the add-on is absent — its bean is declared by a conditional auto-configuration.
 */
public class AiToolsLlmDataQueryService implements LlmDataQueryService {

    @Autowired
    protected EntityDataLoadGenerationService entityDataLoadGenerationService;

    @Autowired
    protected JpqlExecutionService jpqlExecutionService;

    @Override
    public LlmDataQuery generate(LlmQueryGenerationRequest request) {
        EntityDataLoadQuery generatedQuery;
        try {
            generatedQuery = entityDataLoadGenerationService.generate(composeUserText(request));
        } catch (RuntimeException e) {
            throw new LlmDataQueryException("Cannot generate a query for the data set prompt", e);
        }

        if (StringUtils.isBlank(generatedQuery.getJpql())) {
            throw new LlmDataQueryException("Query generation produced no query text");
        }

        return new LlmDataQuery(generatedQuery.getJpql(), generatedQuery.getResultProperties(),
                toQueryParameters(generatedQuery.getParameters()), generatedQuery.getExplanation(),
                generatedQuery.getWarnings(), generatedQuery.getMaxResults());
    }

    @Override
    public List<Map<String, Object>> execute(LlmQueryExecutionRequest request) {
        LlmDataQuery query = request.getQuery();
        Integer maxResults = request.getMaxResults() != null ? request.getMaxResults() : query.getMaxResults();

        JpqlExecutionResult result = jpqlExecutionService.execute(new JpqlExecutionRequest(
                request.getPrompt(),
                query.getJpql(),
                toExecutionParameters(request.getArguments()),
                query.getResultProperties(),
                maxResults,
                null));

        if (!result.isExecuted()) {
            throw new LlmDataQueryException(describeFailure(result));
        }

        return result.getRows();
    }

    /**
     * Appends the parameter contract to the prompt. It goes into the user text rather than into a prompt
     * provider, because the data-load system prompt is a single bean shared by every consumer of generation.
     * <p>
     * Only the referenced parameters may be declared: the add-on's {@code ParametersValidator} reports a
     * declared parameter the query never uses as an issue.
     */
    protected String composeUserText(LlmQueryGenerationRequest request) {
        StringBuilder userText = new StringBuilder(request.getPrompt());

        if (!request.getAvailableParameters().isEmpty()) {
            userText.append("\n\nAVAILABLE REPORT PARAMETERS:");
            for (LlmQueryParameter parameter : request.getAvailableParameters()) {
                userText.append("\n- :").append(parameter.getName())
                        .append(" (").append(parameter.getJavaType()).append(')');
            }
            userText.append("\n\nPARAMETER RULES:")
                    .append("\n- Reference these as JPQL named parameters, never inline their values.")
                    .append("\n- Declare in \"parameters\" only the ones the query actually references.")
                    .append("\n- Use a parameter only where the request calls for it; ignore the rest.");
        }

        if (request.getMaxResults() != null) {
            userText.append("\n\nROW LIMIT: return at most ").append(request.getMaxResults()).append(" rows.");
        }

        return userText.toString();
    }

    /**
     * Keeps the names and types of the generated parameters and drops their values: the values a model
     * invents belong to the request it was generated for, while the query is stored and reused.
     */
    protected List<LlmQueryParameter> toQueryParameters(List<GeneratedJpqlParameter> generatedParameters) {
        List<LlmQueryParameter> parameters = new ArrayList<>(generatedParameters.size());
        for (GeneratedJpqlParameter generatedParameter : generatedParameters) {
            parameters.add(new LlmQueryParameter(generatedParameter.getName(),
                    StringUtils.defaultString(generatedParameter.getType()), null));
        }
        return parameters;
    }

    protected List<JpqlExecutionParameter> toExecutionParameters(List<LlmQueryParameter> arguments) {
        List<JpqlExecutionParameter> parameters = new ArrayList<>(arguments.size());
        for (LlmQueryParameter argument : arguments) {
            parameters.add(new JpqlExecutionParameter(argument.getName(), argument.getJavaType(),
                    argument.getValue()));
        }
        return parameters;
    }

    protected String describeFailure(JpqlExecutionResult result) {
        String executionError = result.getExecutionError();
        if (StringUtils.isNotBlank(executionError)) {
            return "Cannot execute the query: " + executionError;
        }

        JpqlValidationResult validationResult = result.getValidationResult();
        if (!validationResult.isValid()) {
            return "The query was rejected as invalid: " + validationResult.getIssues().stream()
                    .map(JpqlValidationIssue::getMessage)
                    .collect(Collectors.joining("; "));
        }

        return "The query was not executed";
    }
}
